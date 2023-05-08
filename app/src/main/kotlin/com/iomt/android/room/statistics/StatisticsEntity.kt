package com.iomt.android.room.statistics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.iomt.android.room.basic.BasicEntity
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkEntity
import kotlinx.datetime.LocalDateTime

/**
 * @property timestamp moment when the statistic was taken
 * @property allNumber number of all records present in database
 * @property synchronizedNumber
 * @property deviceCharacteristicLinkId
 */
@Entity(
    tableName = "statistics",
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
data class StatisticsEntity(
    val timestamp: LocalDateTime,
    @ColumnInfo("all_count") val allNumber: Long,
    @ColumnInfo("sync_count") val synchronizedNumber: Long,
    @ColumnInfo(name = "device_char_link_id") val deviceCharacteristicLinkId: Long,
) : BasicEntity()
