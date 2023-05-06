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
    @Query("SELECT * FROM record WHERE is_sync = 0")
    suspend fun getNotSynchronized(): List<RecordEntity>

    /**
     * @param localDateTime
     */
    @Query("DELETE FROM record WHERE is_sync = 1 AND timestamp < :localDateTime")
    suspend fun cleanSynchronizedRecordsOlderThen(localDateTime: LocalDateTime)

    /**
     * @param deviceCharacteristicLinkId [RecordEntity.deviceCharacteristicLinkId]
     * @param localDateTime [LocalDateTime] that should be older than oll the fetched data
     * @param secondsInterval minimal period of data
     * @return [List] of [RecordEntity] of requested DeviceCharacteristicLink that
     *         is older than [localDateTime] and the period is [secondsInterval]
     */
    @Query("""
        SELECT * FROM record 
        WHERE device_char_link_id = :deviceCharacteristicLinkId 
            AND timestamp >= :localDateTime 
            AND is_sync = 1
            AND id IN (
                SELECT MIN(id)
                FROM record 
                WHERE device_char_link_id = :deviceCharacteristicLinkId 
                    AND timestamp >= :localDateTime 
                    AND is_sync = 1
                GROUP BY CAST(julianday(timestamp) * 60 * 60 * 24 / :secondsInterval AS INTEGER)
            )
    """)
    suspend fun getPeriodicalRecordsByLinkIdNotOlderThen(
        deviceCharacteristicLinkId: Long,
        localDateTime: LocalDateTime,
        secondsInterval: Long,
    ): List<RecordEntity>

    /**
     * @return number of all records present in database
     */
    @Query("SELECT COUNT(*) FROM record")
    suspend fun countAll(): Long

    /**
     * @return number of synchronized records present in database
     */
    @Query("SELECT COUNT(*) FROM record WHERE is_sync = 1")
    suspend fun countSynchronized(): Long

    /**
     * @return [Map] where key is [RecordEntity.deviceCharacteristicLinkId] and value is amount of records with this id
     */
    @Query("SELECT device_char_link_id, COUNT(*) FROM record GROUP BY device_char_link_id")
    @MapInfo(
        keyColumn = "device_char_link_id",
        valueColumn = "COUNT(*)",
    )
    suspend fun countAllGroupedByLinkId(): Map<Long, Long>

    /**
     * @return [Map] where key is [RecordEntity.deviceCharacteristicLinkId] and value is amount of records with this id
     */
    @Query("SELECT device_char_link_id, COUNT(*) FROM record WHERE is_sync = 1 GROUP BY device_char_link_id")
    @MapInfo(
        keyColumn = "device_char_link_id",
        valueColumn = "COUNT(*)",
    )
    suspend fun countSynchronizedGroupedByLinkId(): Map<Long, Long>
}
