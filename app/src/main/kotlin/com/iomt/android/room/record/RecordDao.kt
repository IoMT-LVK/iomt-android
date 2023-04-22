package com.iomt.android.room.record

import androidx.room.*
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
     * @return all [RecordEntity] where [RecordEntity.isSynchronized] is false
     */
    @Query("SELECT * FROM record WHERE isSynchronized = 0")
    suspend fun getNotSynchronized(): List<RecordEntity>

    /**
     * @param localDateTime
     */
    @Query("DELETE FROM record WHERE isSynchronized = 1 AND timestamp < :localDateTime")
    suspend fun cleanSynchronizedRecordsOlderThen(localDateTime: LocalDateTime)

    /**
     * @param deviceCharacteristicLinkId [RecordEntity.deviceCharacteristicLinkId]
     * @param localDateTime [LocalDateTime] that should be the bottom border of filtering
     * @return [List] of [RecordEntity]s by [deviceCharacteristicLinkId] that are younger than [localDateTime]
     */
    @Query("SELECT * FROM record WHERE device_char_link_id = :deviceCharacteristicLinkId AND timestamp >= :localDateTime LIMIT $DEFAULT_PAGE_SIZE")
    suspend fun getRecordsByLinkIdNotOlderThen(deviceCharacteristicLinkId: Long, localDateTime: LocalDateTime): List<RecordEntity>

    companion object {
        private const val DEFAULT_PAGE_SIZE = 500
    }
}
