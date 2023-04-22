package com.iomt.android.room.record

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

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
    @Query("SELECT * FROM record LIMIT $DEFAULT_PAGE_SIZE")
    suspend fun getAll(): List<RecordEntity>

    /**
     * @param localDateTime
     */
    @Query("DELETE FROM record WHERE isSynchronized = 1 AND timestamp < :localDateTime")
    suspend fun cleanSynchronizedRecordsOlderThen(localDateTime: LocalDateTime)

    companion object {
        private const val DEFAULT_PAGE_SIZE = 500
    }
}
