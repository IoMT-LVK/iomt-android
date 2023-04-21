package com.iomt.android.configs

import com.iomt.android.room.characteristic.CharacteristicEntity

import java.util.*

import kotlinx.serialization.Serializable

/**
 * @property id backend id of a config file
 * @property general [GeneralConfig] of device
 * @property characteristics [CharacteristicConfig]s that define a set of characteristics
 */
@Serializable
data class DeviceConfig(
    val id: Long,
    val general: GeneralConfig,
    val characteristics: Map<String, CharacteristicConfig>,
) {
    companion object {
        val stub = DeviceConfig(-1, GeneralConfig.stub, mapOf("heartRate" to CharacteristicConfig.stub))
    }
}

/**
 * @return [List] of [CharacteristicEntity]s created with [DeviceConfig.characteristics]
 */
fun Map<String, CharacteristicConfig>.toCharacteristicEntities() = map { (name, config) ->
    CharacteristicEntity(
        name, config.prettyName, UUID.fromString(config.serviceUuid), UUID.fromString(config.characteristicUuid),
    )
}
