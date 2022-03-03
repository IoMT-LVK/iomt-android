package com.iomt.android

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class HTTPRequests(private val context: Context, private val jwt: String? = null, private val userId: String? = null) {
    private val builder = GsonBuilder()
    private val gson = builder.create()

    fun sendDevice(dev: DeviceInfo) {
        val postUrl =
            context.getString(R.string.base_url) + "/devices/register/?token=" + jwt + "&user_id=" + userId
        val requestQueue = Volley.newRequestQueue(context)
        val postData = JSONObject()
        try {
            postData.put("device_id", dev.address)
            postData.put("device_name", dev.name)
            postData.put("device_type", dev.device_type)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            postUrl,
            postData,
            { response: JSONObject? -> }) { error: VolleyError ->
            Log.d(TAG, "HTTP Error: $error")
            error.printStackTrace()
        }
        Log.d(TAG, "Sent")
        requestQueue.add(jsonObjectRequest)
    }

    fun deleteDevice(dev: DeviceInfo) {
        val getUrl =
            context.getString(R.string.base_url) + "/devices/delete/?token=" + jwt + "&user_id=" + userId + "&id=" + dev.address
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            getUrl,
            null,
            { response: JSONObject? -> }) { error: VolleyError ->
            Log.d(TAG, "HTTP Error: $error")
            error.printStackTrace()
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun getDevices(action: Action) {
        val getUrl =
            context.getString(R.string.base_url) + "/devices/get/?token=" + jwt + "&user_id=" + userId
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, getUrl, null, { response: JSONObject ->
                try {
                    val args = arrayOfNulls<String>(1)
                    args[0] = response.getString("devices")
                    Log.d(TAG, args[0]?: "Something went wrong")
                    action.run(args)
                } catch (e: JSONException) {
                    Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.message))
                    e.printStackTrace()
                }
            }) { error: VolleyError ->
                Log.d(TAG, "HTTP Error: $error")
                error.printStackTrace()
            }
        requestQueue.add(jsonObjectRequest)
    }

    fun getDeviceTypes(action: Action) {
        val getUrl =
            context.getString(R.string.base_url) + "/devices/types/?token=" + jwt + "&user_id=" + userId
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, getUrl, null, { response: JSONObject ->
                try {
                    val args = arrayOfNulls<String>(1)
                    args[0] = response.getString("devices")
                    Log.d(TAG, "DevTypes: " + args[0])
                    action.run(args)
                } catch (e: JSONException) {
                    Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.message))
                    e.printStackTrace()
                }
            }) { error: VolleyError ->
                Log.d(TAG, "HTTP Error: $error")
                error.printStackTrace()
            }
        requestQueue.add(jsonObjectRequest)
    }

    fun sendLogin(login: String?, password: String?, success: SuccessAction, error: ErrorAction) {
        val postUrl = context.getString(R.string.base_url) + "/auth/"
        val requestQueue = Volley.newRequestQueue(context)
        val postData = JSONObject()
        try {
            postData.put("login", login)
            postData.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.POST, postUrl, postData, { response: JSONObject ->
                try {
                    val args = arrayOfNulls<String>(2)
                    args[0] = response.getString("jwt")
                    args[1] = response.getString("user_id")
                    success.run(args, response.getBoolean("confirmed"))
                } catch (e: JSONException) {
                    error.run()
                    Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.message))
                    e.printStackTrace()
                }
            }) { err: VolleyError ->
                error.run()
                Log.d(TAG, "HTTP Error: $err")
                err.printStackTrace()
            }
        Log.d(TAG, "Sent")
        requestQueue.add(jsonObjectRequest)
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
        val postUrl = context.getString(R.string.base_url) + "/users/register/"
        val requestQueue = Volley.newRequestQueue(context)
        val postData = JSONObject()
        try {
            postData.put("name", name)
            postData.put("surname", surname)
            postData.put("patronymic", patronymic)
            postData.put("birthdate", birthdate)
            postData.put("email", email)
            postData.put("phone_number", mobile)
            postData.put("login", login)
            postData.put("password", password)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.POST, postUrl, postData, { response: JSONObject ->
                var err: String? = null
                try {
                    err = response.getString("error")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val args = arrayOfNulls<String>(1)
                args[0] = err
                successAction.run(args)
            }) { error: VolleyError ->
                errorAction.run()
                Log.d(TAG, "HTTP Error: $error")
                error.printStackTrace()
            }
        Log.d(TAG, "Sent")
        requestQueue.add(jsonObjectRequest)
    }

    fun getData(action: Action) {
        val getUrl =
            context.getString(R.string.base_url) + "/users/info/?token=" + jwt + "&user_id=" + userId
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, getUrl, null, { response: JSONObject ->
                try {
                    val args = arrayOfNulls<String>(8)
                    args[0] = response.getInt("weight").toString()
                    args[1] = response.getInt("height").toString()
                    args[2] = response["birthdate"] as String
                    args[3] = response["phone_number"] as String
                    args[4] = response["email"] as String
                    args[5] = response["name"] as String
                    args[6] = response["surname"] as String
                    args[7] = response["patronymic"] as String
                    action.run(args)
                } catch (e: JSONException) {
                    Log.d(TAG, "JSON Error: " + Objects.requireNonNull(e.message))
                    e.printStackTrace()
                }
            }) { error: VolleyError ->
                Log.d(TAG, "HTTP Error: $error")
                error.printStackTrace()
            }
        requestQueue.add(jsonObjectRequest)
    }

    fun sendData(
        name: String?,
        surname: String?,
        patr: String?,
        birthdate: String?,
        email: String?,
        phone: String?,
        weight: Int,
        height: Int
    ) {
        val postUrl =
            context.getString(R.string.base_url) + "/users/info/?token=" + jwt + "&user_id=" + userId
        val requestQueue = Volley.newRequestQueue(context)
        val postData = JSONObject()
        try {
            postData.put("name", name)
            postData.put("surname", surname)
            postData.put("birthdate", birthdate)
            postData.put("height", height)
            postData.put("weight", weight)
            postData.put("email", email)
            postData.put("phone_number", phone)
            postData.put("patronymic", patr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            postUrl,
            postData,
            { response: JSONObject? -> }) { error: VolleyError ->
            Log.d(TAG, "HTTP Error: $error")
            error.printStackTrace()
        }
        Log.d(TAG, "Sent")
        requestQueue.add(jsonObjectRequest)
    }

    companion object {
        private const val TAG = "HTTPRequests"
    }
}