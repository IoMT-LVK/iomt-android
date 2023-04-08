package com.iomt.android.room.devicechar

import androidx.room.*

/**
 * Data Access Object of [DeviceCharacteristicLinkEntity]
 */
@Dao
interface DeviceCharacteristicLinkDao {
    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to insert (should have id not null)
     * @return id generated for [deviceCharacteristicLinkEntity]
     */
    @Insert
    suspend fun insert(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity): Long

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to update (should have id not null)
     */
    @Update
    suspend fun update(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity)

    /**
     * @param deviceCharacteristicLinkEntity [DeviceCharacteristicLinkEntity] to delete (should have id not null)
     */
    @Delete
    suspend fun delete(deviceCharacteristicLinkEntity: DeviceCharacteristicLinkEntity)

    /**
     * @param deviceId id of device entity
     * @return [List] of [DeviceCharacteristicLinkRepository] where device id is [deviceId]
     */
    @Transaction
    @Query("SELECT * FROM device_characteristic_link WHERE device_id = :deviceId")
    suspend fun getByDeviceId(deviceId: Long): List<DeviceCharacteristicLinkEntity>

    /**
     * @param deviceId id of device entity
     * @param characteristicId id of characteristic entity
     * @return [DeviceCharacteristicLinkEntity] where device id is [deviceId] and characteristic id is [characteristicId]
     */
    @Transaction
    @Query("SELECT * FROM device_characteristic_link WHERE device_id = :deviceId AND characteristic_id = :characteristicId LIMIT 1")
    fun getByDeviceIdAndCharacteristicId(deviceId: Long, characteristicId: Long): DeviceCharacteristicLinkEntity
}
