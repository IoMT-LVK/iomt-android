package com.iomt.android.config

import com.iomt.android.config.configs.CharacteristicConfig
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.config.configs.GeneralConfig

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig

import kotlinx.serialization.serializer

/**
 * Class that encapsulates the logic of config parsing
 * todo: replace class with set of functions
 */
class ConfigParser {
    /**
     * Lines of toml config
     */
    var tomlLines: List<String> = emptyList()

    /**
     * @return [GeneralConfig] parsed from [tomlLines]
     */
    fun parseGeneralConfig(): GeneralConfig =
        toml.partiallyDecodeFromString(serializer(), tomlLines, "general", tomlConfig)

    private fun parseCharacteristicConfig(characteristicName: String): CharacteristicConfig =
        toml.partiallyDecodeFromString(serializer(), tomlLines, characteristicName, tomlConfig)

    /**
     * @param characteristicNames list of characteristic names
     * @return map, where [characteristicNames] are keys and [CharacteristicConfig]s are values
     */
    @Suppress("MemberVisibilityShouldBePrivate")
    fun parseAllCharacteristicConfigs(characteristicNames: List<String>): Map<String, CharacteristicConfig> =
        characteristicNames.associateWith { parseCharacteristicConfig(it) }

    override fun toString(): String =
        tomlLines.joinToString(separator = "\n")

    /**
     * @param tomlString config file as [String]
     * @return [DeviceConfig] parsed from [tomlString]
     */
    fun parseFromString(tomlString: String): DeviceConfig {
        tomlLines = tomlString.split("\n")
        return parse()
    }

    /**
     * @return [DeviceConfig] parsed from [tomlLines]
     */
    fun parse(): DeviceConfig =
        parseGeneralConfig().let { DeviceConfig(it, parseAllCharacteristicConfigs(it.characteristicNames)) }

    companion object {
        private val tomlConfig = TomlInputConfig(
            ignoreUnknownNames = true,
        )
        private val toml = Toml(tomlConfig)
    }
}
