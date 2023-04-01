package com.iomt.android.ui.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.iomt.android.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.util.*

/**
 * Fragment that is responsible for device displaying
 */
@Deprecated("Deprecated due to Jetpack Compose Migration")
class DeviceFragment : Fragment() {
    private var mqttAndroidClient: MqttAndroidClient? = null
    private var senderService: SenderService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        senderService = SenderService(requireContext(), 1)
        mqttAndroidClient = MqttAndroidClient(requireContext(), "${R.string.base_uri}:${R.string.mqtt_port}", "")
        mqttAndroidClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                Log.d(loggerTag, "Connection was lost!")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.d(loggerTag, "Message Arrived!: $topic: ${String(message.payload)}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d(loggerTag, "Delivery Complete!")
            }
        })
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.BLUETOOTH] != true) {
                    // permission was granted, proceed with the operation that requires the permission
                    throw IllegalStateException("Requested permission was not granted")
                }
            }.launch(arrayOf(Manifest.permission.BLUETOOTH))
        }
    }

    /**
     * @param mqttAndroidClient
     * @param data
     */
    fun sendData(mqttAndroidClient: MqttAndroidClient, data: JSONObject) {
        val prefs = requireContext().getSharedPreferences(
            requireContext().getString(R.string.ACC_DATA),
            Context.MODE_PRIVATE
        )
        val jwt = prefs.getString("JWT", "")
        val userId = prefs.getString("UserId", "")
        val deviceId = prefs.getString("DeviceId", "")
        val options = MqttConnectOptions().apply {
            userName = "username"
            password = jwt!!.toCharArray()
        }
        try {
            mqttAndroidClient.connect(options, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(loggerTag, "Connection Success!")
                    try {
                        val dataString = data.toString()
                        val message = MqttMessage(dataString.toByteArray())
                        Log.d(loggerTag, "Publishing message$message")
                        message.qos = 2
                        message.isRetained = false
                        mqttAndroidClient.publish("c/$userId/$deviceId/data", message)
                        // SenderService.this.dbhelper.deleteNote(ids.get(i));
                    } catch (ex: MqttException) {
                        ex.printStackTrace()
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(loggerTag, "Connection Failure!")
                    Log.d(loggerTag, exception.toString())
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    companion object {
        private val heartRateMeasurementServiceUuid =
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

        private val heartRateMeasurementCharacteristicUuid =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

        private val respirationServiceUuid =
            UUID.fromString("3b55c581-bc19-48f0-bd8c-b522796f8e24")

        private val respirationRateMeasurementCharacteristicUuid =
            UUID.fromString("9bc730c3-8cc0-4d87-85bc-573d6304403c")

        private val clientCharacteristicConfigUuid =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val accelerometerServiceUuid =
            UUID.fromString("bdc750c7-2649-4fa8-abe8-fbf25038cda3")

        private val accelerometerMeasurementCharacteristicUuid =
            UUID.fromString("75246a26-237a-4863-aca6-09b639344f43")
        private val loggerTag = DeviceFragment::class.java.simpleName
    }
}
