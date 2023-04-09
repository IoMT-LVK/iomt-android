package com.iomt.android.room.device

import android.content.Context
import com.iomt.android.room.AppDatabase

/**
 * Repository for [DeviceEntity]
 */
class DeviceRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val dao = appDatabase.deviceDao()

    /**
     * @param deviceEntity [DeviceEntity] to insert
     * @return id generated for [deviceEntity]
     */
    suspend fun insert(deviceEntity: DeviceEntity): Long = dao.insert(deviceEntity)

    /**
     * @param deviceEntity [DeviceEntity] to update (should have id not null)
     */
    suspend fun update(deviceEntity: DeviceEntity) = dao.update(deviceEntity)

    /**
     * @param deviceEntity [DeviceEntity] to delete (should have id not null)
     */
    suspend fun delete(deviceEntity: DeviceEntity) = dao.delete(deviceEntity)

    /**
     * @param name device name
     * @param mac device MAC address
     */
    suspend fun getByMacOrSave(name: String, mac: String) = dao.getByMacOrSave(mac, name)
}
