package com.iomt.android.entities

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.widget.TextView

data class Characteristic(
    val textView: TextView,
    var bluetoothGattService: BluetoothGattService? = null,
    var bluetoothGattCharacteristic: BluetoothGattCharacteristic? = null,
) {
    var isUpdated: Boolean = false
}