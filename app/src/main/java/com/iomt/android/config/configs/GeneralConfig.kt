package com.iomt.android.config.configs

import kotlinx.serialization.Serializable

@Serializable
data class GeneralConfig(
    var name: String,
    var nameRegex: String,
    var characteristicNames: List<String>,
    var type: String? = null,
)