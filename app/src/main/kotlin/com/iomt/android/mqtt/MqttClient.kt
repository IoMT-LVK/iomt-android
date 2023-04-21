package com.iomt.android.mqtt

import android.content.Context
import android.util.Log

import com.iomt.android.http.authenticate
import com.iomt.android.utils.ExpiringValueWrapper

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Class that wraps [MqttAndroidClient] in order to initialize MQTT connection and send data
 */
class MqttClient(context: Context) {
    private val mqttClient = MqttAndroidClient(context, MQTT_BROKER_URL, MQTT_USER_ID)
    private val token = ExpiringValueWrapper { getAuthorizationToken() }

    /**
     * Connect to MQTT broker
     *
     * @return [IMqttToken]
     */
    suspend fun connect(): IMqttToken {
        /**
         * Server expects token as username
         * Password is not required
         */
        val mqttConnectOptions = MqttConnectOptions().apply {
            userName = token.getValue()
        }
        return mqttClient.connect(mqttConnectOptions)
    }

    /**
     * @param topic [Topic] to send data
     * @param message message to publish
     * @return [IMqttDeliveryToken]
     * @throws IllegalStateException on no [mqttClient] connection
     */
    fun send(topic: Topic, message: ByteArray): IMqttDeliveryToken {
        val mqttMessage = MqttMessage(message).apply {
            qos = 2
            isRetained = false
        }

        return if (mqttClient.isConnected) {
            mqttClient.publish(topic.toTopicName(), mqttMessage)
        } else {
            throw IllegalStateException("MqttClient is not connected")
        }
    }

    /**
     * Disconnect [mqttClient]
     *
     * @return [IMqttToken]
     */
    fun disconnect(): IMqttToken = mqttClient.disconnect().also {
        it.waitForCompletion()
        Log.d(loggerTag, "Disconnected")
    }

    private suspend fun getAuthorizationToken(): Pair<String, LocalDateTime> = simpleHttpClient.authenticate().run {
        token to expires.toInstant().toLocalDateTime(TimeZone.UTC)
    }

    companion object {
        private val simpleHttpClient = HttpClient(Android) {
            install(ContentNegotiation) { json() }
        }
        private val loggerTag = MqttClient::class.java.simpleName

        private const val MQTT_BROKER_URL = "tcp://iomt.lvk.cs.msu.ru:1883"

        /**
         * TODO: ensure there is no need in user id for mqtt on server side
         */
        private const val MQTT_USER_ID = ""
    }
}
