package com.iomt.android.mqtt

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * @property value
 * @property timestamp
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
