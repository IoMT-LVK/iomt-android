package com.iomt.android.room.devicechar

import androidx.room.*
import com.iomt.android.room.basic.BasicEntity
import com.iomt.android.room.characteristic.CharacteristicEntity
import com.iomt.android.room.device.DeviceEntity

/**
 * @property deviceId id of [DeviceEntity]
 * @property characteristicId id of [CharacteristicEntity]
 */
@Entity(
    tableName = "device_characteristic_link",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CharacteristicEntity::class,
            parentColumns = ["id"],
            childColumns = ["characteristic_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("device_id"),
        Index("characteristic_id")
    ],
)
data class DeviceCharacteristicLinkEntity(
    @ColumnInfo(name = "device_id") val deviceId: Long,
    @ColumnInfo(name = "characteristic_id") val characteristicId: Long,
) : BasicEntity()
