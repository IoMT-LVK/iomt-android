package com.iomt.android

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.iomt.android.config.configs.DeviceConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

@Serializable
data class AuthInfo(
        val jwt: String,
        // snake case due to the format of data
        val user_id: String,
        val confirmed: Boolean,
)

class HTTPRequests(
        private val context: Context,
        private var jwt: String? = null,
        private var userId: String? = null,
){
    private val loggingInterceptor = HttpLoggingInterceptor {
        Log.d(TAG, it)
    }.apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }
    private val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    private val coroutineForRequests = CoroutineScope(Job())

    fun sendDevice(dev: DeviceInfo) {
        val postUrl = "${context.getString(R.string.base_url)}/devices/register/?token=$jwt&user_id=$userId"
        val json = JSONObject().apply {
            put("device_id", dev.address)
            put("device_name", dev.name)
            put("device_type", dev.device_type)
        }.toString().toRequestBody(JSON)
        val request = Request.Builder().url(postUrl).post(json).build()
        coroutineForRequests.launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Request <$postUrl> was successfully sent.")
            } else {
                Log.e(TAG, "Got [${response.code}] from server with <$postUrl>.")
            }
        }
    }

    fun deleteDevice(dev: DeviceInfo) {
        val getUrl = "${context.getString(R.string.base_url)}/devices/delete/?token=$jwt&user_id=$userId&id=${dev.address}"
        val request = Request.Builder().url(getUrl).get().build()
        coroutineForRequests.launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Request <$getUrl> was successfully sent.")
            } else {
                Log.e(TAG, "Got [${response.code}] from server with <$getUrl>.")
            }
        }
    }

    fun getDevices(action: Action) {
        Log.w(TAG, "JWT: $jwt, userID: $userId")
        val getUrl = "${context.getString(R.string.base_url)}/devices/get/?token=$jwt&user_id=$userId"
        val request = Request.Builder().url(getUrl).get().build()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Request <$getUrl> was successfully sent.")
                    action.run(arrayOf(JSONObject(response.body!!.string()).getString("devices")))
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$getUrl>.")
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Got [${e.localizedMessage}] from server with <$getUrl>.")
            }
        })
    }

    fun getDeviceTypes(action: Action) {
        val getUrl = "${context.getString(R.string.base_url)}/devices/types/?token=$jwt&user_id=$userId"
        val request = Request.Builder().url(getUrl).get().build()
        coroutineForRequests.launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Request <$getUrl> was successfully sent.")
                action.run(arrayOf(JSONObject(response.body!!.string()).getString("devices")))
            } else {
                Log.e(TAG, "Got [${response.code}] from server with <$getUrl>.")
            }
        }
    }

    fun sendLogin(login: String?, password: String?, success: SuccessAction, error: ErrorAction) {
        val postUrl = "${context.getString(R.string.base_url)}/auth/"
        val json = JSONObject().apply {
            put("login", login ?: "")
            put("password", password ?: "")
        }.toString().toRequestBody(JSON)
        val request = Request.Builder()
                .url(postUrl)
                .post(json)
                .addHeader("Content-Type", "application/json")
                .build()
        coroutineForRequests.launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Request <$postUrl> was successfully sent.")
                val stringResponse = response.body?.string() ?: ""
                val jsonStringResponse = JSONObject(stringResponse)
                Log.w(TAG, jsonStringResponse.getString("jwt"))
                val authInfo = Json.decodeFromString<AuthInfo>(stringResponse)
                jwt = authInfo.jwt.also { Log.w(TAG, "jwt: $it") }
                userId = authInfo.user_id.also { Log.w(TAG, "userId: $it") }

                val auth = arrayOf(jwt, userId)
                val isConfirmed = jsonStringResponse.getBoolean("confirmed")
                success.run(auth, isConfirmed)
            } else {
                Log.e(TAG, "Got [${response.code}] from server with <$postUrl>.")
                error.run()
            }
        }
    }

    fun sendReg(
        name: String?,
        surname: String?,
        patronymic: String?,
        birthdate: String?,
        email: String?,
        mobile: String?,
        login: String?,
        password: String?,
        successAction: Action,
        errorAction: ErrorAction
    ) {
        val postUrl = "${context.getString(R.string.base_url)}/users/register/"
        val json = JSONObject().apply {
            put("name", name!!)
            put("surname", surname!!)
            put("patronymic", patronymic!!)
            put("birthdate", birthdate!!)
            put("email", email!!)
            put("phone_number", mobile!!)
            put("login", login!!)
            put("password", password!!)
        }.toString().toRequestBody(JSON)
        val request = Request.Builder().url(postUrl).post(json).build()
        client.newCall(request).enqueue(
            object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Request <$postUrl> was successfully sent.")
                        val err = JSONObject(response.body!!.string()).getString("error")
                        successAction.run(arrayOf(err))
                    } else {
                        Log.e(TAG, "Got [${response.code}] from server with <$postUrl>.")
                        errorAction.run()
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Got [${e.localizedMessage}] from server with <$postUrl>.")
                    errorAction.run()
                }
            }
        )
    }

    fun getData(action: Action) {
        val getUrl = "${context.getString(R.string.base_url)}/users/info/?token=$jwt&user_id=$userId"
        val request = Request.Builder()
                .url(getUrl)
                .get()
                .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Request <$getUrl> was successfully sent.")
                    val jsonStringResponse = JSONObject(response.body!!.string())
                    action.run(arrayOf(
                        jsonStringResponse.getInt("weight").toString(),
                        jsonStringResponse.getInt("height").toString(),
                        jsonStringResponse["birthdate"] as String,
                        jsonStringResponse["phone_number"] as String,
                        jsonStringResponse["email"] as String,
                        jsonStringResponse["name"] as String,
                        jsonStringResponse["surname"] as String,
                        jsonStringResponse["patronymic"] as String,
                    ))
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$getUrl>.")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$getUrl>.")
            }
        })
    }

    fun sendData(
        name: String?,
        surname: String?,
        patronymic: String?,
        birthdate: String?,
        email: String?,
        phone: String?,
        weight: Int,
        height: Int
    ) {
        val postUrl = "${context.getString(R.string.base_url)}/users/info/?token=$jwt&user_id=$userId"
        val json = JSONObject().apply {
            put("name", name!!)
            put("surname", surname!!)
            put("birthdate", birthdate!!)
            put("height", height.toString())
            put("weight", weight.toString())
            put("email", email!!)
            put("phone_number", phone!!)
            put("patronymic", patronymic!!)
        }.toString().toRequestBody(JSON)
        val request = Request.Builder().url(postUrl).post(json).build()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Request <$postUrl> was successfully sent.")
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$postUrl>.")
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$postUrl>.")
            }
        })

    }

    fun getDeviceConfig(configId: Long, ): String? {
        var deviceStringConfig: String? = null
        val saveConfig: (String) -> Unit = {
            deviceStringConfig = it
        }
        val url = "${context.getString(R.string.base_url)}/devices/types?id=$configId&token=$jwt"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    saveConfig(response.body!!.string())
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$url>.")
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$url>.")
            }
        })

        return deviceStringConfig
    }

    companion object {
        private const val TAG = "HTTPRequests"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}