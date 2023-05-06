package com.iomt.android.room.statistics

import androidx.room.Dao
import androidx.room.Query
import com.iomt.android.room.basic.BasicDao
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object of [StatisticsEntity]
 */
@Dao
interface StatisticsDao : BasicDao<StatisticsEntity> {
    /**
     * @return all [StatisticsEntity]s as [Flow]
     */
    @Query("SELECT * FROM statistics")
    fun getAll(): Flow<StatisticsEntity>
}
