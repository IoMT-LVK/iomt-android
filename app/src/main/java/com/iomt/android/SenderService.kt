package com.iomt.android

import android.content.Context
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.iomt.android.utils.getValueByColumnName
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Class used to send medical data to server using MQTT
 *
 * @property context
 * @property delay
 */
class SenderService(private val context: Context, private val delay: Int) {
    private var dbhelper: DatabaseHelper? = null
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val runnableCode: Runnable = object : Runnable {
        @Suppress("NESTED_BLOCK", "TOO_LONG_FUNCTION")
        override fun run() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (!connectivityManager.isConnectedToNetwork()) {
                return
            }
            val mqttAndroidClient = MqttAndroidClient(context, URL, "")
            mqttAndroidClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable) {
                    Log.d(TAG, "Connection was lost!")
                }
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d(TAG, "Message Arrived!: $topic: ${String(message.payload)}")
                }
                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d(TAG, "Delivery Complete!")
                }
            })
            dbhelper = DatabaseHelper(context)
            val db = dbhelper!!.readableDatabase
            val cursor = db.query(Note.TABLE_NAME, null, null, null, null, null, Note.COLUMN_TIMESTAMP, null)

            // looping through all rows and adding to list
            val res: MutableList<JSONObject> = ArrayList()
            val ids: MutableList<Int> = ArrayList()
            var cnt = 0
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    cnt++
                    val result = JSONObject()
                    try {
                        sequenceOf<Pair<String, Cursor.(Int) -> Any>>(
                            Note.COLUMN_HEART_RATE to { getInt(it) },
                            Note.COLUMN_RESP_RATE to { getInt(it) },
                            Note.COLUMN_INSP to { getFloat(it) },
                            Note.COLUMN_EXP to { getFloat(it) },
                            Note.COLUMN_CADENCE to { getInt(it) },
                            Note.COLUMN_STEP_COUNT to { getInt(it) },
                            Note.COLUMN_ACT to { getFloat(it) },
                            Note.COLUMN_CLI to { getString(it) },
                            Note.COLUMN_MIL to { getInt(it) },
                        ).map { (columnName, getter) -> result.put(columnName, cursor.getValueByColumnName(columnName, getter)) }

                        res.add(result)
                        ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(Note.COLUMN_ID)))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            sendMedicalData(mqttAndroidClient, res, ids)
            Log.d(TAG, cnt.toString())
            // close db connection
            db.close()
            handler.postDelayed(this, delay.toLong())
        }
    }

    private fun ConnectivityManager.isConnectedToMobile() = getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.state == NetworkInfo.State.CONNECTED
    private fun ConnectivityManager.isConnectedToWifi() = getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.state == NetworkInfo.State.CONNECTED
    private fun ConnectivityManager.isConnectingOrConnected() = activeNetworkInfo?.isConnectedOrConnecting ?: false
    private fun ConnectivityManager.isConnectedToNetwork() = (isConnectedToMobile() || isConnectedToWifi()) && isConnectingOrConnected()

    /**
     * Start sending
     */
    fun start() {
        handler.postDelayed(runnableCode, 5000)
    }

    /**
     * Stop sending
     */
    fun stop() {
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * @param mqttAndroidClient
     * @param data
     * @param ids
     */
    fun sendMedicalData(mqttAndroidClient: MqttAndroidClient, data: List<JSONObject>, ids: List<Int>) {
        val prefs = context.getSharedPreferences(context.getString(R.string.ACC_DATA), Context.MODE_PRIVATE)
        val jwt = requireNotNull(prefs.getString("JWT", "")) { "JWT token should not be null" }
        val userId = requireNotNull(prefs.getString("UserId", "")) { "userId should not be null" }
        val deviceId = requireNotNull(prefs.getString("DeviceId", "")) { "deviceId should not be null" }
        val options = MqttConnectOptions().apply {
            userName = "username"
            password = jwt.toCharArray()
        }
        try {
            mqttAndroidClient.connect(options, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "Connection Success!")
                    try {
                        for (i in data.indices) {
                            val dataStr = data[i].toString()
                            val message = MqttMessage(dataStr.toByteArray())
                            Log.d(TAG, "Publishing message: [$message]")
                            message.qos = 2
                            message.isRetained = false
                            mqttAndroidClient.publish("c/$userId/$deviceId/data", message)
                            dbhelper!!.deleteNote(ids[i])
                            Thread.sleep(200)
                        }
                    } catch (ex: MqttException) {
                        ex.printStackTrace()
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(TAG, "Connection Failure!")
                    Log.d(TAG, exception.toString())
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }
    companion object {
        private const val TAG = "SenderService"
        private const val URL = "tcp://iomt.lvk.cs.msu.ru:8883"
    }
}
