package com.iomt.android.room.device

import androidx.room.*
import com.iomt.android.room.basic.BasicDao
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object of [DeviceEntity]
 */
@Dao
interface DeviceDao : BasicDao<DeviceEntity> {
    /**
     * @return all [DeviceEntity]s as [Flow]
     */
    @Query("SELECT * FROM device")
    fun getAll(): Flow<DeviceEntity>

    /**
     * @param deviceId id of [DeviceEntity]
     * @return [DeviceEntity] by [deviceId]
     */
    @Query("SELECT * FROM device WHERE id = :deviceId LIMIT 1")
    suspend fun getById(deviceId: Long): DeviceEntity?

    /**
     * @param mac mac address of device
     * @return [DeviceEntity] by [mac]
     */
    @Query("SELECT * FROM device WHERE mac = :mac LIMIT 1")
    suspend fun getByMac(mac: String): DeviceEntity?

    /**
     * @param mac device mac address
     * @param name device name
     * @return [DeviceEntity] by mac address if it is present in database, newly saved [DeviceEntity] otherwise
     */
    suspend fun getByMacOrSave(mac: String, name: String): DeviceEntity = getByMac(mac) ?: DeviceEntity(name, mac).let { device ->
        device.apply { this.id = insert(this) }
    }
}
