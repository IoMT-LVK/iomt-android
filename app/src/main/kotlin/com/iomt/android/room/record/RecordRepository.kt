package com.iomt.android.room.record

import android.content.Context
import com.iomt.android.room.AppDatabase
import kotlinx.datetime.LocalDateTime

/**
 * Repository for [RecordEntity]
 */
class RecordRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val dao = appDatabase.recordDao()

    /**
     * @param recordEntity [RecordEntity] to insert
     * @return id generated for [recordEntity]
     */
    suspend fun insert(recordEntity: RecordEntity): Long = dao.insert(recordEntity)

    /**
     * @param recordEntity [RecordEntity] to update (should have id not null)
     */
    suspend fun update(recordEntity: RecordEntity) = dao.update(recordEntity)

    /**
     * @param recordEntity [RecordEntity] to delete (should have id not null)
     */
    suspend fun delete(recordEntity: RecordEntity) = dao.delete(recordEntity)

    /**
     * @return all [RecordEntity] where [RecordEntity.isSynchronized] is false
     */
    suspend fun getNotSynchronized(): List<RecordEntity> = dao.getNotSynchronized()

    /**
     * @param localDateTime
     */
    suspend fun cleanSynchronizedRecordsOlderThen(
        localDateTime: LocalDateTime,
    ): Unit = dao.cleanSynchronizedRecordsOlderThen(localDateTime)

    /**
     * @param deviceCharacteristicLinkId
     * @param localDateTime
     */
    suspend fun getRecordsByLinkIdNotOlderThen(
        deviceCharacteristicLinkId: Long,
        localDateTime: LocalDateTime,
    ) = dao.getRecordsByLinkIdNotOlderThen(deviceCharacteristicLinkId, localDateTime)
}
