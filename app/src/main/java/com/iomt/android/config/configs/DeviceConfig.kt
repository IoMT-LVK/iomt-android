package com.iomt.android.config.configs

import kotlinx.serialization.Serializable

/**
 * @property general [GeneralConfig] of device
 * @property characteristics [CharacteristicConfig]s that define a set of characteristics
 */
@Serializable
data class DeviceConfig(
    val general: GeneralConfig,
    val characteristics: Map<String, CharacteristicConfig>,
)
