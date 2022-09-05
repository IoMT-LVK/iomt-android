package com.iomt.android

import android.content.Context
import android.util.Log

import com.iomt.android.entities.AuthInfo

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

import java.io.IOException

import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Class that is used to perform requests.
 */
class Requests(
    private val context: Context,
    private var jwt: String? = null,
    private var userId: String? = null,
) {
    private val loggingInterceptor = HttpLoggingInterceptor {
        Log.d(TAG, it)
    }.apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }
    private val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    private val coroutineForRequests = CoroutineScope(Job())

    /**
     * @param device [DeviceInfo] that will be sent
     */
    fun sendDevice(device: DeviceInfo) {
        val postUrl = "${context.getString(R.string.base_url)}/devices/register/?token=$jwt&user_id=$userId"
        val json = JSONObject().apply {
            put("device_id", device.address)
            put("device_name", device.name)
            put("device_type", device.deviceType)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(postUrl)
            .post(json)
            .build()
        coroutineForRequests.launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Request <$postUrl> was successfully sent.")
            } else {
                Log.e(TAG, "Got [${response.code}] from server with <$postUrl>.")
            }
        }
    }

    /**
     * @param device [DeviceInfo] of device that will be deleted
     */
    fun deleteDevice(device: DeviceInfo) {
        val getUrl = "${context.getString(R.string.base_url)}/devices/delete/?token=$jwt&user_id=$userId&id=${device.address}"
        val request = Request.Builder()
            .url(getUrl)
            .get()
            .build()
        coroutineForRequests.launch {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "Request <$getUrl> was successfully sent.")
            } else {
                Log.e(TAG, "Got [${response.code}] from server with <$getUrl>.")
            }
        }
    }

    /**
     * @param action
     */
    fun getDevices(action: Action) {
        Log.w(TAG, "JWT: $jwt, userID: $userId")
        val getUrl = "${context.getString(R.string.base_url)}/devices/get/?token=$jwt&user_id=$userId"
        val request = Request.Builder()
            .url(getUrl)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Request <$getUrl> was successfully sent.")
                    action.run(arrayOf(JSONObject(response.body!!.string()).getString("devices")))
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$getUrl>.")
                }
            }
            @Suppress("IDENTIFIER_LENGTH")
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$getUrl>.")
            }
        })
    }

    /**
     * @param action
     */
    fun getDeviceTypes(action: Action) {
        val getUrl = "${context.getString(R.string.base_url)}/devices/types/?token=$jwt&user_id=$userId"
        val request = Request.Builder()
            .url(getUrl)
            .get()
            .build()
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

    /**
     * @param login
     * @param password
     * @return [AuthInfo]
     */
    fun sendLogin(login: String, password: String) = runBlocking {
        val url = "${context.getString(R.string.base_url)}/auth/"
        val json = JSONObject().apply {
            put("login", login)
            put("password", password)
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(json)
            .addHeader("Content-Type", "application/json")
            .build()
        val deferred: CompletableDeferred<AuthInfo> = CompletableDeferred()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val stringResponse = response.body?.string()
                if (response.isSuccessful && stringResponse != null) {
                    deferred.complete(Json.decodeFromString(stringResponse))
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$url>.")
                    deferred.complete(AuthInfo("", "", false, wasFailed = true))
                }
            }
            @Suppress("IDENTIFIER_LENGTH")
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$url>.")
                deferred.complete(AuthInfo("", "", false, wasFailed = true))
            }
        })
        deferred.await()
    }

    /**
     * @param name
     * @param surname
     * @param patronymic
     * @param birthdate
     * @param email
     * @param mobile
     * @param login
     * @param password
     * @param successAction
     * @param errorAction
     */
    @Suppress("TOO_MANY_PARAMETERS")
    fun sendReg(
        name: String?,
        surname: String?,
        patronymic: String?,
        birthdate: String?,
        email: String?,
        mobile: String?,
        login: String?,
        password: String?,
        errorAction: ErrorAction,
        successAction: Action,
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
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(postUrl)
            .post(json)
            .build()
        client.newCall(request).enqueue(
            object : Callback {
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
                @Suppress("IDENTIFIER_LENGTH")
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Got [${e.localizedMessage}] from server with <$postUrl>.")
                    errorAction.run()
                }
            }
        )
    }

    /**
     * @param action
     */
    fun getData(action: Action) {
        val getUrl = "${context.getString(R.string.base_url)}/users/info/?token=$jwt&user_id=$userId"
        val request = Request.Builder()
            .url(getUrl)
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
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
            @Suppress("IDENTIFIER_LENGTH")
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$getUrl>.")
            }
        })
    }

    /**
     * @param name
     * @param surname
     * @param patronymic
     * @param birthdate
     * @param email
     * @param phone
     * @param weight
     * @param height
     */
    @Suppress("TOO_MANY_PARAMETERS")
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
        }.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(postUrl)
            .post(json)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Request <$postUrl> was successfully sent.")
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$postUrl>.")
                }
            }
            @Suppress("IDENTIFIER_LENGTH")
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$postUrl>.")
            }
        })
    }

    /**
     * @param configId
     * @return [String] of device config
     */
    fun getDeviceConfig(configId: Long): String = runBlocking {
        val url = "${context.getString(R.string.base_url)}/devices/types?id=$configId&token=$jwt"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val deferred: CompletableDeferred<String> = CompletableDeferred()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val config = response.body?.string()
                if (response.isSuccessful && response.body != null) {
                    deferred.complete(config ?: "")
                } else {
                    Log.e(TAG, "Got [${response.code}] from server with <$url>.")
                    deferred.complete("")
                }
            }
            @Suppress("IDENTIFIER_LENGTH")
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Got [${e.localizedMessage}] from server with <$url>.")
                deferred.complete("")
            }
        })
        deferred.await()
    }

    companion object {
        private const val TAG = "HTTPRequests"
        private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    }
}
