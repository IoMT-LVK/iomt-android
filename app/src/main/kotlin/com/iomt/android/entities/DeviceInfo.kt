package com.iomt.android.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class that contains info about BLE-device
 *
 * @property name
 * @property address
 * @property deviceType
 */
@Serializable
data class DeviceInfo(
    @SerialName("device_name") val name: String,
    @SerialName("device_id") val address: String,
    @SerialName("device_type") val deviceType: String,
) {
    companion object {
        val stubDeviceList = listOf(
            DeviceInfo("Device 1", "1", "band"),
            DeviceInfo("Device 2", "2", "hexoskin"),
            DeviceInfo("Device 3", "3", "band")
        ).toTypedArray()
    }
}
