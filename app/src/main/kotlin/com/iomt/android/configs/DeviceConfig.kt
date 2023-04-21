package com.iomt.android.configs

import com.iomt.android.entities.Characteristic
import com.iomt.android.room.characteristic.CharacteristicEntity

import java.util.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property general [GeneralConfig] of device
 * @property characteristics [CharacteristicConfig]s that define a set of characteristics
 */
@Serializable
data class DeviceConfig(
    val general: GeneralConfig,
    @SerialName("sensors") val characteristics: List<CharacteristicConfig>,
) {
    companion object {
        val stub = DeviceConfig(GeneralConfig.stub, listOf(CharacteristicConfig.stub))
    }
}

/**
 * @return [List] of [Characteristic] created with [DeviceConfig.characteristics]
 */
fun List<CharacteristicConfig>.toCharacteristics(): List<Characteristic> = map {
    Characteristic(it.name, it.prettyName)
}

/**
 * @return [List] of [CharacteristicEntity]s created with [DeviceConfig.characteristics]
 */
fun List<CharacteristicConfig>.toCharacteristicEntities() = map {
    CharacteristicEntity(
        it.name, it.prettyName, UUID.fromString(it.serviceUuid), UUID.fromString(it.characteristicUuid),
    )
}
