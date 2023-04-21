package com.iomt.android.room.characteristic

import androidx.room.*

import com.iomt.android.room.basic.BasicDao

import java.util.*

import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object of [CharacteristicEntity]
 */
@Dao
interface CharacteristicDao : BasicDao<CharacteristicEntity> {
    /**
     * @return all [CharacteristicEntity]s as [Flow]
     */
    @Query("SELECT * FROM characteristic")
    fun getAll(): Flow<CharacteristicEntity>

    /**
     * @param name characteristic name
     * @param characteristicUuid [UUID] of BluetoothGattCharacteristic corresponding to this characteristic
     * @return [CharacteristicEntity] with [name] and [characteristicUuid]
     */
    @Query("SELECT * FROM characteristic WHERE name = :name AND char_uuid = :characteristicUuid LIMIT 1")
    suspend fun getByNameAndCharacteristicUuid(name: String, characteristicUuid: UUID): CharacteristicEntity?

    /**
     * @param ids [List] of [CharacteristicEntity] IDs
     * @return [List] of [CharacteristicEntity] with [ids]
     */
    @Query("SELECT * FROM characteristic WHERE id IN (:ids)")
    suspend fun getByIdsIn(ids: List<Long>): List<CharacteristicEntity>

    /**
     * @param id ID of [CharacteristicEntity]
     * @return [CharacteristicEntity] with [id]
     */
    @Query("SELECT * FROM characteristic WHERE id = :id")
    suspend fun getById(id: Long): CharacteristicEntity?
}
