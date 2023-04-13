package com.iomt.android.config.configs

import com.iomt.android.entities.Characteristic
import com.iomt.android.room.characteristic.CharacteristicEntity

import java.util.*

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

/**
 * @return [List] of [Characteristic] created with [DeviceConfig.characteristics]
 */
fun Map<String, CharacteristicConfig>.toCharacteristics(): List<Characteristic> = map { (charName, charConfig) ->
    Characteristic(charName, charConfig.name)
}

/**
 * @return [List] of [CharacteristicEntity]s created with [DeviceConfig.characteristics]
 */
fun Map<String, CharacteristicConfig>.toCharacteristicEntities() = map { (charName, charConfig) ->
    CharacteristicEntity(
        charName, charConfig.name, UUID.fromString(charConfig.serviceUuid), UUID.fromString(charConfig.characteristicUuid),
    )
}
