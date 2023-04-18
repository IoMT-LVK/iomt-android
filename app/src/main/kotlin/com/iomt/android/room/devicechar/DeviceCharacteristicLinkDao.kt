package com.iomt.android.room.devicechar

import androidx.room.*
import com.iomt.android.room.basic.BasicDao

/**
 * Data Access Object of [DeviceCharacteristicLinkEntity]
 */
@Dao
interface DeviceCharacteristicLinkDao : BasicDao<DeviceCharacteristicLinkEntity> {
    /**
     * @param deviceId id of device entity
     * @return [List] of [DeviceCharacteristicLinkRepository] where device id is [deviceId]
     */
    @Transaction
    @Query("SELECT * FROM device_characteristic_link WHERE device_id = :deviceId")
    suspend fun getByDeviceId(deviceId: Long): List<DeviceCharacteristicLinkEntity>

    /**
     * @param id id of [DeviceCharacteristicLinkEntity]
     * @return [DeviceCharacteristicLinkEntity] with [id]
     */
    @Query("SELECT * FROM device_characteristic_link WHERE id = :id")
    suspend fun getById(id: Long): DeviceCharacteristicLinkEntity?

    /**
     * @param deviceId id of device entity
     * @param characteristicId id of characteristic entity
     * @return [DeviceCharacteristicLinkEntity] where device id is [deviceId] and characteristic id is [characteristicId]
     */
    @Transaction
    @Query("SELECT * FROM device_characteristic_link WHERE device_id = :deviceId AND characteristic_id = :characteristicId LIMIT 1")
    fun getByDeviceIdAndCharacteristicId(deviceId: Long, characteristicId: Long): DeviceCharacteristicLinkEntity?
}
