package com.iomt.android.room.device

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.iomt.android.room.basic.BasicEntity

/**
 * @property name device name
 * @property mac device MAC address
 */
@Entity(tableName = "device")
data class DeviceEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "mac") val mac: String,
) : BasicEntity()
