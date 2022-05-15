package com.iomt.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SenderService(ctx: Context, mdelay: Int) {
    private val TAG = "SenderService"
    private val url = "tcp://iomt.lvk.cs.msu.ru:8883"
    private var dbhelper: DatabaseHelper? = null
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val delay: Int = mdelay
    private val context: Context = ctx
    fun start() {
        handler.postDelayed(runnableCode, 5000)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
    }

    private val runnableCode: Runnable = object : Runnable {
        override fun run() {
            var connected = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!.state == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
            ) {
                val activeNetwork = connectivityManager.activeNetworkInfo
                connected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting
            }
            if (!connected) {
                return
            }
            val mqttAndroidClient = MqttAndroidClient(context, url, "")
            mqttAndroidClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable) {
                    Log.d(TAG, "Connection was lost!")
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d(TAG, "Message Arrived!: " + topic + ": " + String(message.payload))
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d(TAG, "Delivery Complete!")
                }
            })
            dbhelper = DatabaseHelper(context)
            val db = dbhelper!!.readableDatabase
            val cursor =
                db.query(Note.TABLE_NAME, null, null, null, null, null, Note.COLUMN_TIMESTAMP, null)

            // looping through all rows and adding to list
            val res: MutableList<JSONObject> = ArrayList()
            val ids: MutableList<Int> = ArrayList()
            var cnt = 0
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    cnt++
                    val result = JSONObject()
                    try {
                        result.put(
                            "HeartRate",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_HEART_RATE)))  {
                                JSONObject.NULL
                            } else {
                                cursor.getInt(cursor.getColumnIndex(Note.COLUMN_HEART_RATE))
                            }
                        )
                        result.put(
                            "RespRate",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_RESP_RATE))) JSONObject.NULL else cursor.getInt(
                                cursor.getColumnIndex(Note.COLUMN_RESP_RATE)
                            )
                        )
                        result.put(
                            "Insp",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_INSP))) JSONObject.NULL else cursor.getFloat(
                                cursor.getColumnIndex(
                                    Note.COLUMN_INSP
                                )
                            )
                        )
                        result.put(
                            "Exp",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_EXP))) JSONObject.NULL else cursor.getFloat(
                                cursor.getColumnIndex(
                                    Note.COLUMN_EXP
                                )
                            )
                        )
                        result.put(
                            "Cadence",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_CADENCE))) JSONObject.NULL else cursor.getInt(
                                cursor.getColumnIndex(
                                    Note.COLUMN_CADENCE
                                )
                            )
                        )
                        result.put(
                            "Steps",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT))) JSONObject.NULL else cursor.getInt(
                                cursor.getColumnIndex(
                                    Note.COLUMN_STEP_COUNT
                                )
                            )
                        )
                        result.put(
                            "Activity",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_ACT))) JSONObject.NULL else cursor.getFloat(
                                cursor.getColumnIndex(
                                    Note.COLUMN_ACT
                                )
                            )
                        )
                        result.put(
                            "Clitime",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_CLI))) JSONObject.NULL else cursor.getString(
                                cursor.getColumnIndex(
                                    Note.COLUMN_CLI
                                )
                            )
                        )
                        result.put(
                            "Millisec",
                            if (cursor.isNull(cursor.getColumnIndex(Note.COLUMN_MIL))) JSONObject.NULL else cursor.getInt(
                                cursor.getColumnIndex(
                                    Note.COLUMN_MIL
                                )
                            )
                        )
                        res.add(result)
                        ids.add(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)))
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
            send_data(mqttAndroidClient, res, ids)
            Log.d(TAG, cnt.toString())
            // close db connection
            db.close()
            handler.postDelayed(this, delay.toLong())
        }
    }

    fun send_data(mqttAndroidClient: MqttAndroidClient, data: List<JSONObject>, ids: List<Int>) {
        val prefs =
            context.getSharedPreferences(context.getString(R.string.ACC_DATA), Context.MODE_PRIVATE)
        val JWT = prefs.getString("JWT", "")
        val UserId = prefs.getString("UserId", "")
        val DeviceId = prefs.getString("DeviceId", "")
        val options = MqttConnectOptions()
        options.userName = "username"
        options.password = JWT!!.toCharArray()
        try {
            mqttAndroidClient.connect(options, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "Connection Success!")
                    try {
                        for (i in data.indices) {
                            val data_str = data[i].toString()
                            val message = MqttMessage(data_str.toByteArray())
                            Log.d(TAG, "Publishing message$message")
                            message.qos = 2
                            message.isRetained = false
                            mqttAndroidClient.publish("c/$UserId/$DeviceId/data", message)
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
}