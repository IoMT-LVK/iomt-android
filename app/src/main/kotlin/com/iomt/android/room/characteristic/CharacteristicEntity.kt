package com.iomt.android.room.characteristic

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.iomt.android.room.basic.BasicEntity
import java.util.*

/**
 * @property name characteristic name
 * @property prettyName human-readable characteristic name
 * @property serviceUuid [UUID] of BluetoothGattService corresponding to this characteristic
 * @property characteristicUuid [UUID] of BluetoothGattCharacteristic corresponding to this characteristic
 */
@Entity(tableName = "characteristic")
data class CharacteristicEntity(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "pretty_name") val prettyName: String,
    @ColumnInfo(name = "service_uuid") val serviceUuid: UUID,
    @ColumnInfo(name = "char_uuid") val characteristicUuid: UUID,
) : BasicEntity()
