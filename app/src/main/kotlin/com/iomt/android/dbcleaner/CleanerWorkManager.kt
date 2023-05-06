package com.iomt.android.dbcleaner

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

/**
 * Class that is responsible for [WorkManager] initialization for record data cleanup
 */
class CleanerWorkManager(context: Context) {
    private val manager = WorkManager.getInstance(context)

    /**
     * @param ttl [Duration] that defines synchronized data time to live - if data is older, it should be cleaned up
     * @param repeatIntervalDuration period for [PeriodicWorkRequest] as [Duration]
     * @return initialized [Operation]
     */
    suspend fun start(
        ttl: Duration = 1.days,
        repeatIntervalDuration: Duration = 1.days,
    ) = manager.enqueueUniquePeriodicWork(
        cleanerWorkManagerClassName,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        getWorkRequest(ttl, repeatIntervalDuration),
    ).also {
        Log.i(cleanerWorkManagerClassName, "Starting work...")
        it.await()
        Log.i(cleanerWorkManagerClassName, "Successfully sent start request")
    }

    /**
     * @return stopped [Operation]
     */
    suspend fun stop() = manager.cancelUniqueWork(cleanerWorkManagerClassName).also {
        Log.i(cleanerWorkManagerClassName, "Stopping...")
        it.await()
        Log.i(cleanerWorkManagerClassName, "Successfully stopped")
    }

    companion object {
        private val cleanerWorkManagerClassName = CleanerWorkManager::class.java.simpleName

        @Volatile
        private var instance: CleanerWorkManager? = null

        /**
         * @param context [Context]
         * @return [CleanerWorkManager]
         */
        fun getInstance(context: Context): CleanerWorkManager {
            synchronized(this) {
                instance ?: run { instance = CleanerWorkManager(context) }
                return instance!!
            }
        }

        private fun getWorkRequest(
            ttl: Duration,
            repeatIntervalDuration: Duration,
        ) = PeriodicWorkRequestBuilder<CleanerWorker>(repeatIntervalDuration.toLong(DurationUnit.MINUTES), TimeUnit.MINUTES)
            .setInputData(Data.Builder().putLong("dataTtlMinutes", ttl.toLong(DurationUnit.MINUTES)).build())
            .addTag(cleanerWorkManagerClassName)
            .build()
    }
}
