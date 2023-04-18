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
    private val worker = WorkManager.getInstance(context)

    /**
     * TODO: support changing [repeatIntervalDuration]
     *
     * @param repeatIntervalDuration period for [PeriodicWorkRequest] as [Duration]
     * @param networkType required network to use
     * @return initialized [Operation]
     */
    suspend fun start(
        repeatIntervalDuration: Duration = 5.minutes,
        networkType: NetworkType = NetworkType.UNMETERED,
    ) = worker.enqueueUniquePeriodicWork(
        mqttWorkManagerClassName,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        getWorkRequest(repeatIntervalDuration, networkType),
    ).also {
        Log.i(mqttWorkManagerClassName, "Starting work...")
        it.await()
        Log.i(mqttWorkManagerClassName, "Successfully sent start request")
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
            repeatIntervalDuration: Duration,
            networkType: NetworkType = NetworkType.UNMETERED,
        ) = PeriodicWorkRequestBuilder<MqttWorker>(repeatIntervalDuration.toLong(DurationUnit.MINUTES), TimeUnit.MINUTES)
            .setConstraints(
                Constraints(
                    requiredNetworkType = networkType,
                    requiresBatteryNotLow = true,
                )
            )
            .addTag(mqttWorkManagerClassName)
            .build()
    }
}
