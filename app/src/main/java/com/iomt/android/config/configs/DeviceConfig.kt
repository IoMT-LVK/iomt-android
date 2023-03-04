package com.iomt.android.config.configs

/**
 * @property general [GeneralConfig] of device
 * @property characteristics [CharacteristicConfig]s that define a set of characteristics
 */
data class DeviceConfig(
    val general: GeneralConfig,
    val characteristics: Map<String, CharacteristicConfig>,
)
