package com.iomt.android.room.devicechar

import com.iomt.android.room.characteristic.CharacteristicEntity
import com.iomt.android.room.device.DeviceEntity

/**
 * Data class that represents [DeviceCharacteristicLinkEntity] mapping for device with [DeviceCharacteristicLinkEntity.deviceId]
 *
 * @property deviceEntity [DeviceEntity]
 * @property characteristicEntities [List] of [CharacteristicEntity]s corresponding to [deviceEntity]
 */
data class DeviceCharacteristicLink(
    val deviceEntity: DeviceEntity,
    val characteristicEntities: Map<CharacteristicEntity, Long>,
) {
    /**
     * @param characteristicName name of characteristic
     * @return id of link
     */
    fun getLinkIdByCharacteristicName(characteristicName: String) = characteristicEntities.toList()
        .find { (char, _) -> char.name == characteristicName }
        ?.let { (_, id) -> id }
        .let { id -> requireNotNull(id) { "Could not find characteristic by name $characteristicName" } }
}
