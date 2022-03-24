package com.iomt.android.config

import kotlinx.serialization.decodeFromString

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlConfig
import com.iomt.android.config.configs.CharacteristicConfig
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.config.configs.GeneralConfig
import kotlinx.serialization.serializer
import java.io.File
import java.net.URL


class ConfigParser(pathToToml: String) {
    private val tomlConfig = TomlConfig(
        ignoreUnknownNames = true,
    )
    private var tomlLines: List<String> = readFromConfig(pathToToml)

    private fun readFromConfig(path: String): List<String> = File(path).bufferedReader().use { it.readLines() }

//    private fun readFromUrl(url: String): List<String> = URL(url).readText().split("\n")

    fun parseGeneralConfig(): GeneralConfig =
        Toml(tomlConfig).partiallyDecodeFromString(serializer(), tomlLines, "general")

    private fun parseCharacteristicConfig(characteristicName: String): CharacteristicConfig =
        Toml(tomlConfig).partiallyDecodeFromString(serializer(), tomlLines, characteristicName)

    fun parseAllCharacteristicConfigs(characteristicNames: List<String>): Map<String, CharacteristicConfig> =
        characteristicNames.map { it to parseCharacteristicConfig(it) }.toMap()

    override fun toString(): String =
        tomlLines.joinToString(separator = "\n")

    fun parse(): DeviceConfig =
        parseGeneralConfig().let { DeviceConfig(it, parseAllCharacteristicConfigs(it.characteristicNames)) }
}