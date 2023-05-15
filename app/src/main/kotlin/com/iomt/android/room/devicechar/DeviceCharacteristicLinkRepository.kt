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
     * @param entities [List] of [DeviceCharacteristicLinkEntity] to save
     * @return [List] of ids generated for [entities]
     */
    suspend fun insertAllIfNotPresent(entities: List<DeviceCharacteristicLinkEntity>): List<Long> = entities.map { entity ->
        dao.getByDeviceIdAndCharacteristicId(entity.deviceId, entity.characteristicId)?.id ?: dao.insert(entity)
    }

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to update (should have id not null)
     */
    suspend fun update(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity) = dao.update(deviceCharacteristicLinkEntity)

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to delete (should have id not null)
     */
    suspend fun delete(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity) = dao.delete(deviceCharacteristicLinkEntity)

    /**
     * @param macAddress MAC address of device
     * @return [List] of characteristic entities linked with device with [macAddress]
     */
    suspend fun getLinkByDeviceMac(macAddress: String): DeviceCharacteristicLink? {
        val device = deviceDao.getByMac(macAddress) ?: return null
        val links = dao.getByDeviceId(device.id!!)
        val characteristics = characteristicDao.getByIdsIn(links.map { it.characteristicId })
        val characteristicMap = characteristics.associateWith { characteristic ->
            links.first { link -> link.characteristicId == characteristic.id }.id!!
        }
        return DeviceCharacteristicLink(device, characteristicMap)
    }

    /**
     * @param linkEntityId id of [DeviceCharacteristicLinkEntity]
     * @return mac address of device and characteristic name of characteristic mentioned in
     *         [DeviceCharacteristicLinkEntity] with [linkEntityId]
     */
    suspend fun getDeviceMacAndCharacteristicNameByLinkId(linkEntityId: Long): Pair<String, String>? {
        val deviceCharacteristicLinkEntity = dao.getById(linkEntityId) ?: return null
        val deviceEntity = deviceDao.getById(deviceCharacteristicLinkEntity.deviceId) ?: return null
        val characteristicEntity = characteristicDao.getById(deviceCharacteristicLinkEntity.characteristicId) ?: return null
        return deviceEntity.mac to characteristicEntity.name
    }
}
