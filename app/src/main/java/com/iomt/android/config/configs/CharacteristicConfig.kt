package com.iomt.android.config.configs

import kotlinx.serialization.Serializable


@Serializable
data class CharacteristicConfig (
    var name: String,
    var serviceUUID: String? = null,
    var characteristicUUID: String? = null,
    var descriptorUUID: String? = null,
//  var formula: String? = null
)