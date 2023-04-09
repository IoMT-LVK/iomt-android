package com.iomt.android.room.record

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object of [RecordEntity]
 */
@Dao
interface RecordDao {
    /**
     * @param recordEntity
     * @return generated id for [recordEntity]
     */
    @Insert
    suspend fun insert(recordEntity: RecordEntity): Long

    /**
     * @param recordEntity [RecordEntity] to update (should have id not null)
     */
    @Update
    suspend fun update(recordEntity: RecordEntity)

    /**
     * @param recordEntity [RecordEntity] to delete (should have id not null)
     */
    @Delete
    suspend fun delete(recordEntity: RecordEntity)

    /**
     * @return all [RecordEntity] as [Flow]
     */
    @Query("SELECT * FROM record")
    fun getAll(): Flow<RecordEntity>
}
