package com.iomt.android.room.statistics

import android.content.Context
import com.iomt.android.room.AppDatabase

/**
 * Repository for [StatisticsEntity]
 */
class StatisticsRepository(context: Context) {
    private val appDatabase = AppDatabase.getInstance(context)
    private val dao = appDatabase.statisticsDao()

    /**
     * @param statisticsEntities [List] of [StatisticsEntity] to insert
     * @return Unit
     */
    suspend fun insertAll(statisticsEntities: List<StatisticsEntity>): List<Long> = statisticsEntities.map {
        dao.insert(it)
    }
}
