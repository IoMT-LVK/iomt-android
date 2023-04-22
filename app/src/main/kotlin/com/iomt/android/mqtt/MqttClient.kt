package com.iomt.android.mqtt

import android.util.Log

import com.iomt.android.http.authenticate
import com.iomt.android.utils.ExpiringValueWrapper

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

import java.util.UUID

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Class that wraps [com.hivemq.client.mqtt.MqttClient] in order to initialize MQTT connection and send data
 */
class MqttClient {
    private val mqttClient = try {
        com.hivemq.client.mqtt.MqttClient.builder()
            .useMqttVersion3()
            .identifier(UUID.randomUUID().toString())
            .serverHost(MQTT_BROKER_HOST)
            .serverPort(MQTT_BROKER_PORT)
            .build()
            .toBlocking()
    } catch (exception: Exception) {
        Log.e(loggerTag, "Error occurred", exception)
        throw exception
    }
    private val token = ExpiringValueWrapper { getAuthorizationToken() }

    /**
     * Connect to MQTT broker
     *
     * @return [Mqtt3ConnAck]
     * @throws Exception
     */
    suspend fun connect(): Mqtt3ConnAck {
        val tokenValue = token.getValue()
        return try {
            mqttClient.connectWith()
                .simpleAuth()
                .username(tokenValue)
                .password(tokenValue.toByteArray())
                .applySimpleAuth()
                .send()
        } catch (exception: Exception) {
            Log.e(loggerTag, "Error occurred", exception)
            exception.printStackTrace()
            throw exception
        }
    }

    /**
     * @param topic [Topic] to send data
     * @param message message to publish
     * @throws IllegalStateException on no [mqttClient] connection
     * @throws Exception
     */
    fun send(topic: Topic, message: MqttRecordMessage) {
        val publishMessage = Mqtt3Publish.builder()
            .topic(topic.toTopicName())
            .payload(message.toByteArray())
            .qos(MqttQos.EXACTLY_ONCE)
            .retain(false)
            .build()
        return try {
            if (mqttClient.state.isConnected) {
                mqttClient.publish(publishMessage)
            } else {
                throw IllegalStateException("MqttClient is not connected")
            }
        } catch (exception: Exception) {
            Log.e(loggerTag, "Error occurred", exception)
            exception.printStackTrace()
            throw exception
        }
    }

    /**
     * Disconnect [mqttClient]
     *
     * @return [Unit]
     */
    fun disconnect(): Unit = mqttClient.disconnect().also {
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

        private const val MQTT_BROKER_HOST = "iomt.lvk.cs.msu.ru"
        private const val MQTT_BROKER_PORT = 1883
    }
}
