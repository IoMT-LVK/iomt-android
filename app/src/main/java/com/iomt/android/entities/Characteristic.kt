package com.iomt.android.entities

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.widget.TextView

/**
 * @property textView text field that should be displayed
 * @property bluetoothGattService [BluetoothGattService] corresponding to [Characteristic]
 * @property bluetoothGattCharacteristic [BluetoothGattCharacteristic] corresponding to [Characteristic]
 * @property isUpdated flag that defines if characteristic was updated or not
 */
data class Characteristic(
    val textView: TextView,
    val bluetoothGattService: BluetoothGattService? = null,
    val bluetoothGattCharacteristic: BluetoothGattCharacteristic? = null,
    var isUpdated: Boolean = false,
)
