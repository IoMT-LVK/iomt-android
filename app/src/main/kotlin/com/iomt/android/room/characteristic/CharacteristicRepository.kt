package com.iomt.android.room.characteristic

import android.content.Context

import com.iomt.android.room.AppDatabase

import java.util.UUID

/**
 * Repository for [CharacteristicEntity]
 */
class CharacteristicRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val characteristicDao = appDatabase.characteristicDao()

    /**
     * @param characteristicEntity [CharacteristicEntity] to insert
     * @return id generated for [characteristicEntity]
     */
    suspend fun insert(characteristicEntity: CharacteristicEntity): Long = characteristicDao.insert(characteristicEntity)

    /**
     * @param characteristicEntities [List] of [CharacteristicEntity] to insert
     * @return [List] of id generated for [characteristicEntities]
     */
    suspend fun insertAllIfNotPresent(characteristicEntities: List<CharacteristicEntity>): List<Long> = characteristicEntities.map {
        characteristicDao.getByNameAndCharacteristicUuid(it.name, it.characteristicUuid)?.id ?: characteristicDao.insert(it)
    }

    /**
     * @param characteristicEntity [CharacteristicEntity] to update (should have id not null)
     */
    suspend fun update(characteristicEntity: CharacteristicEntity) = characteristicDao.update(characteristicEntity)

    /**
     * @param characteristicEntity [CharacteristicEntity] to update (should have id not null)
     */
    suspend fun delete(characteristicEntity: CharacteristicEntity) = characteristicDao.delete(characteristicEntity)

    /**
     * @param name characteristic name
     * @param characteristicUuid [UUID] of BluetoothGattCharacteristic corresponding to this characteristic
     * @return [CharacteristicEntity] with [name] and [characteristicUuid]
     */
    suspend fun getByNameAndCharacteristicUuid(
        name: String,
        characteristicUuid: UUID,
    ): CharacteristicEntity? = characteristicDao.getByNameAndCharacteristicUuid(name, characteristicUuid)
}
