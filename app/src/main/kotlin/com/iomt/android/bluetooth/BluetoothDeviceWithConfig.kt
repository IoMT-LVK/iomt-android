package com.iomt.android.bluetooth

import android.bluetooth.BluetoothDevice
import com.iomt.android.configs.DeviceConfig

/**
 * @property device [BluetoothDevice]
 * @property config [DeviceConfig] selected for [device]
 */
data class BluetoothDeviceWithConfig(
    val device: BluetoothDevice,
    val config: DeviceConfig,
)
