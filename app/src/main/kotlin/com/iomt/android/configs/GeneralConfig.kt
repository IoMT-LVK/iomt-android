package com.iomt.android.configs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name name of device
 * @property nameRegex regex that is used for device search, should match the name of device
 * @property type type of device
 */
@Serializable
data class GeneralConfig(
    var name: String,
    @SerialName("name_regex") var nameRegex: String,
    var type: DeviceType,
) {
    companion object {
        val stub = GeneralConfig("Mi Band", ".*", DeviceType.VEST)
    }
}
