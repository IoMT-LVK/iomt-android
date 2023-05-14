package com.iomt.android.statsitics

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/**
 * Class that is responsible for [WorkManager] initialization for statistics recording
 */
class StatisticsWorkManager(context: Context) {
    private val manager = WorkManager.getInstance(context)

    /**
     * @param ttl [Duration] that defines synchronized data time to live - if data is older, it should be cleaned up
     * @return initialized [Operation]
     */
    suspend fun start(
        ttl: Duration = 30.minutes,
    ) = manager.enqueueUniquePeriodicWork(
        statisticsWorkManagerClassName,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        getWorkRequest(ttl),
    ).also {
        Log.i(statisticsWorkManagerClassName, "Starting work...")
        it.await()
        Log.i(statisticsWorkManagerClassName, "Successfully sent start request")
    }

    /**
     * @return stopped [Operation]
     */
    suspend fun stop() = manager.cancelUniqueWork(statisticsWorkManagerClassName).also {
        Log.i(statisticsWorkManagerClassName, "Stopping...")
        it.await()
        Log.i(statisticsWorkManagerClassName, "Successfully stopped")
    }

    companion object {
        private val statisticsWorkManagerClassName = StatisticsWorkManager::class.java.simpleName

        @Volatile
        private var instance: StatisticsWorkManager? = null

        /**
         * @param context [Context]
         * @return [StatisticsWorkManager]
         */
        fun getInstance(context: Context): StatisticsWorkManager {
            synchronized(this) {
                instance ?: run { instance = StatisticsWorkManager(context) }
                return instance!!
            }
        }

        private fun getWorkRequest(
            repeatIntervalDuration: Duration,
        ) = PeriodicWorkRequestBuilder<StatisticsWorker>(repeatIntervalDuration.toLong(DurationUnit.MINUTES), TimeUnit.MINUTES)
            .addTag(statisticsWorkManagerClassName)
            .build()
    }
}
