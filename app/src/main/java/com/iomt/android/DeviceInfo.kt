package com.iomt.android

/**
 * Data class that contains info about BLE-device
 *
 * @property name
 * @property address
 * @property deviceType
 */
data class DeviceInfo(
    val name: String,
    val address: String,
    val deviceType: String,
)
