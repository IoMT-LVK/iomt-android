package com.iomt.android.config.configs

import kotlinx.serialization.Serializable

/**
 * @property name name of device
 * @property nameRegex regex that is used for device search, should match the name of device
 * @property characteristicNames list of characteristic names that are expected to be supported by device
 * @property type type of device
 */
@Serializable
data class GeneralConfig(
    var name: String,
    var nameRegex: String,
    var characteristicNames: List<String>,
    var type: String? = null,
)
