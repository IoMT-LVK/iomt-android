package com.iomt.android.room.record

import androidx.room.*
import com.iomt.android.mqtt.MqttRecordMessage
import com.iomt.android.room.basic.BasicEntity
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * @property deviceCharacteristicLinkId id of [DeviceCharacteristicLinkEntity]
 * @property timestamp [LocalDateTime] of record
 * @property value received value
 * @property isSynchronized flag that defines if the data was sent to MQTT broker or not
 */
@Entity(
    tableName = "record",
    foreignKeys = [
        ForeignKey(
            entity = DeviceCharacteristicLinkEntity::class,
            parentColumns = ["id"],
            childColumns = ["device_char_link_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("device_char_link_id"),
    ],
)
@Serializable
data class RecordEntity(
    @ColumnInfo(name = "device_char_link_id") val deviceCharacteristicLinkId: Long,
    val timestamp: LocalDateTime,
    val value: String,
    @ColumnInfo(name = "is_sync", defaultValue = "0") var isSynchronized: Boolean = false,
) : BasicEntity() {
    /**
     * @return [MqttRecordMessage] from this [RecordEntity]
     */
    fun toMqttRecordMessage() = MqttRecordMessage(value, timestamp)
}
