package com.iomt.android.room.devicechar

import android.content.Context
import com.iomt.android.room.AppDatabase

/**
 * Repository for [DeviceCharacteristicLinkEntity]
 */
class DeviceCharacteristicLinkRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val dao = appDatabase.deviceCharacteristicLinkDao()
    private val deviceDao = appDatabase.deviceDao()
    private val characteristicDao = appDatabase.characteristicDao()

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to insert
     * @return id generated for [deviceCharacteristicLinkEntity]
     */
    suspend fun insert(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity): Long = dao.insert(deviceCharacteristicLinkEntity)

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to update (should have id not null)
     */
    suspend fun update(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity) = dao.update(deviceCharacteristicLinkEntity)

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to delete (should have id not null)
     */
    suspend fun delete(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity) = dao.delete(deviceCharacteristicLinkEntity)

    /**
     * @param deviceId id of device entity
     * @return [List] of characteristic entities linked with device with [deviceId]
     */
    suspend fun getLinksByDeviceId(deviceId: Long): DeviceCharacteristicLink? {
        val device = deviceDao.getById(deviceId) ?: return null
        val links = dao.getByDeviceId(deviceId)
        val characteristics = characteristicDao.getByIdsIn(links.map { it.characteristicId })
        val characteristicMap = characteristics.associateWith { characteristic ->
            links.first { link -> link.characteristicId == characteristic.id }.id!!
        }
        return DeviceCharacteristicLink(device, characteristicMap)
    }
}
