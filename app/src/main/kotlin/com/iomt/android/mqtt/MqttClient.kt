package com.iomt.android.mqtt

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Class that wraps [MqttAndroidClient] in order to initialize MQTT connection and send data
 */
class MqttClient(context: Context) {
    private val mqttClient = MqttAndroidClient(context, MQTT_BROKER_URL, MQTT_USER_ID)

    /**
     * Connect to MQTT broker
     *
     * @return [IMqttToken]
     */
    fun connect(): IMqttToken {
        /**
         * Server expects token as username
         * Password is not required
         */
        val mqttConnectOptions = MqttConnectOptions().apply {
            userName = getAuthorizationToken()
        }
        return mqttClient.connect(mqttConnectOptions)
    }

    /**
     * @param topicName name of a topic to send data
     * @param message message to publish
     * @return [IMqttDeliveryToken]
     * @throws IllegalStateException on no [mqttClient] connection
     */
    fun send(topicName: String, message: ByteArray): IMqttDeliveryToken {
        val mqttMessage = MqttMessage(message).apply {
            qos = 2
            isRetained = false
        }

        return if (mqttClient.isConnected) {
            mqttClient.publish(topicName, mqttMessage)
        } else {
            throw IllegalStateException("MqttClient is not connected")
        }
    }

    /**
     * Disconnect [mqttClient]
     *
     * @return [IMqttToken]
     */
    fun disconnect(): IMqttToken = mqttClient.disconnect()

    /**
     * TODO: should return JWT token
     */
    private fun getAuthorizationToken(): String = ""

    companion object {
        private const val MQTT_BROKER_URL = "tcp://iomt.lvk.cs.msu.ru:1883"

        /**
         * TODO: ensure there is no need in user id for mqtt on server side
         */
        private const val MQTT_USER_ID = ""
    }
}
