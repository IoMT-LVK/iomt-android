package com.iomt.android

/**
 * Data class that contains info about BLE-device
 */
data class DeviceInfo(
    val name: String,
    val address: String,
    val deviceType: String,
)