/**
 * File that contains toml parsing logic
 */

package com.iomt.android.config

import com.iomt.android.config.configs.CharacteristicConfig
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.config.configs.GeneralConfig

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig

import kotlinx.serialization.serializer

private const val GENERAL_SECTION_NAME = "general"

private val tomlConfiguration = TomlInputConfig(ignoreUnknownNames = true)
private val toml = Toml(tomlConfiguration)

/**
 * @param tomlConfig device configuration in toml format
 * @return [DeviceConfig] parsed from [tomlConfig]
 */
fun parseConfig(tomlConfig: String): DeviceConfig = parseGeneralConfig(tomlConfig).let {
    DeviceConfig(it, parseAllCharacteristicConfigs(tomlConfig, it.characteristicNames))
}

private fun parseGeneralConfig(tomlConfig: String): GeneralConfig = toml.partiallyDecodeFromString(
    serializer(),
    tomlConfig,
    GENERAL_SECTION_NAME,
    tomlConfiguration,
)

private fun parseCharacteristicConfig(tomlConfig: String, characteristicName: String): CharacteristicConfig = toml.partiallyDecodeFromString(
    serializer(),
    tomlConfig,
    characteristicName,
    tomlConfiguration,
)

private fun parseAllCharacteristicConfigs(tomlConfig: String, characteristicNames: List<String>): Map<String, CharacteristicConfig> =
    characteristicNames.associateWith { parseCharacteristicConfig(tomlConfig, it) }
