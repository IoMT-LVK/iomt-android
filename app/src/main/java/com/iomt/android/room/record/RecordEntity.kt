package com.iomt.android.room.record

import androidx.room.*
import com.iomt.android.room.basic.BasicEntity
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkEntity
import kotlinx.datetime.LocalDateTime

/**
 * @property deviceCharacteristicLinkId id of [DeviceCharacteristicLinkEntity]
 * @property timestamp [LocalDateTime] of record
 * @property value received value
 */
@Entity(
    tableName = "record",
    foreignKeys = [
        ForeignKey(
            entity = DeviceCharacteristicLinkEntity::class,
            parentColumns = ["id"],
            childColumns = ["device_char_link_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("device_char_link_id"),
    ],
)
data class RecordEntity(
    @ColumnInfo(name = "device_char_link_id") val deviceCharacteristicLinkId: Long,
    val timestamp: LocalDateTime,
    val value: String,
) : BasicEntity()
