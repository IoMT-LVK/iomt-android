package com.iomt.android.mqtt

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data class that represents message that should be sent to server with MQTT
 *
 * @property value recorded value
 * @property timestamp [LocalDateTime] when [value] was received
 */
@Serializable
data class MqttRecordMessage(
    val value: String,
    val timestamp: LocalDateTime,
) {
    /**
     * @return [ByteArray] from [MqttRecordMessage]
     */
    fun toByteArray() = Json.encodeToString(this).toByteArray()
}
