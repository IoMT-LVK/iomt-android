package com.iomt.android

import android.bluetooth.BluetoothDevice

/**
 * @property device
 */
data class DeviceCell(var device: BluetoothDevice) : AbstractCell()
