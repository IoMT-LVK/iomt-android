package com.iomt.android.room.record

import android.content.Context

import com.iomt.android.room.AppDatabase
import com.iomt.android.room.statistics.StatisticsEntity
import com.iomt.android.utils.now

import kotlin.time.Duration
import kotlin.time.DurationUnit
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
     * @param deviceCharacteristicLinkId [RecordEntity.deviceCharacteristicLinkId]
     * @param localDateTime [LocalDateTime] that should be older than oll the fetched data
     * @param periodDuration minimal period of data as [Duration]
     * @return [List] of [RecordEntity] of requested DeviceCharacteristicLink that
     *         is older than [localDateTime] and the period is [secondsInterval]
     */
    suspend fun getPeriodicalRecordsByLinkIdNotOlderThen(
        deviceCharacteristicLinkId: Long,
        localDateTime: LocalDateTime,
        periodDuration: Duration,
    ) = dao.getPeriodicalRecordsByLinkIdNotOlderThen(
        deviceCharacteristicLinkId,
        localDateTime,
        periodDuration.toLong(DurationUnit.SECONDS),
    )

    /**
     * @return number of all records present in database
     */
    suspend fun countAll() = dao.countAll()

    /**
     * @return number of synchronized records present in database
     */
    suspend fun countSynchronized() = dao.countSynchronized()

    /**
     * @return [List] of [StatisticsEntity]
     */
    suspend fun collectStatistics(): List<StatisticsEntity> {
        val allStats = dao.countAllGroupedByLinkId()
        val synchronizedStats = dao.countSynchronizedGroupedByLinkId()
        val now = LocalDateTime.now()
        return allStats.map { (key, value) -> StatisticsEntity(now, value, synchronizedStats[key] ?: 0, key) }
    }
}
