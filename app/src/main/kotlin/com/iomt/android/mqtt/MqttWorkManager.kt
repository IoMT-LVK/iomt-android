package com.iomt.android.mqtt

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/**
 * Class that is responsible for [WorkManager] initialization for data synchronization with [MqttClient]
 */
class MqttWorkManager(context: Context) {
    private val manager = WorkManager.getInstance(context)

    /**
     * TODO: support changing [repeatIntervalDuration]
     *
     * @param userId id of currently logged-in user
     * @param repeatIntervalDuration period for [PeriodicWorkRequest] as [Duration]
     * @param networkType required network to use
     * @return initialized [Operation]
     */
    suspend fun start(
        userId: Long,
        repeatIntervalDuration: Duration = 5.minutes,
        networkType: NetworkType = NetworkType.UNMETERED,
    ) = manager.enqueueUniquePeriodicWork(
        mqttWorkManagerClassName,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        getWorkRequest(userId, repeatIntervalDuration, networkType),
    ).also {
        Log.i(mqttWorkManagerClassName, "Starting work...")
        it.await()
        Log.i(mqttWorkManagerClassName, "Successfully sent start request")
    }

    /**
     * @return stopped [Operation]
     */
    suspend fun stop() = manager.cancelUniqueWork(mqttWorkManagerClassName).also {
        Log.i(mqttWorkManagerClassName, "Stopping...")
        it.await()
        Log.i(mqttWorkManagerClassName, "Successfully stopped")
    }

    companion object {
        private val mqttWorkManagerClassName = MqttWorkManager::class.java.simpleName

        @Volatile
        private var instance: MqttWorkManager? = null

        /**
         * @param context [Context]
         * @return [MqttWorkManager]
         */
        fun getInstance(context: Context): MqttWorkManager {
            synchronized(this) {
                instance ?: run { instance = MqttWorkManager(context) }
                return instance!!
            }
        }

        private fun getWorkRequest(
            userId: Long,
            repeatIntervalDuration: Duration,
            networkType: NetworkType = NetworkType.UNMETERED,
        ) = PeriodicWorkRequestBuilder<MqttWorker>(repeatIntervalDuration.toLong(DurationUnit.MINUTES), TimeUnit.MINUTES)
            .setConstraints(
                Constraints(
                    requiredNetworkType = networkType,
                    requiresBatteryNotLow = true,
                ),
            )
            .setInputData(Data.Builder().putLong("userId", userId).build())
            .addTag(mqttWorkManagerClassName)
            .build()
    }
}
