package com.iomt.android.mqtt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

import com.iomt.android.EntryPointActivity
import com.iomt.android.R
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.room.record.RecordRepository

import org.eclipse.paho.client.mqttv3.MqttException

import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Class that implements [CoroutineWorker] in order to define data synchronization using [MqttClient]
 */
class MqttWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    private val deviceCharacteristicLinkRepository = DeviceCharacteristicLinkRepository(context)
    private val recordRepository = RecordRepository(context)
    private val mqttClient = MqttClient(context)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        val recordsToSend = try {
            recordRepository.getAll().map { recordEntity ->
                Log.d(loggerTag, Json.encodeToString(recordEntity))
                requireNotNull(deviceCharacteristicLinkRepository.getDeviceMacByLinkId(recordEntity.id!!)) {
                    "Could not find device mac in database"
                } to recordEntity
            }
        } catch (exception: IllegalArgumentException) {
            Log.e(loggerTag, "Error while fetching records from database", exception)
            return Result.failure()
        }

        try {
            mqttClient.connect().also {
                it.waitForCompletion(connectionTimeoutDuration.toLong(DurationUnit.MILLISECONDS))
            }
        } catch (mqttException: MqttException) {
            Log.e(loggerTag, "Could not connect to MQTT broker", mqttException)
            return Result.failure()
        }

        recordsToSend.map { (macAddress, recordEntity) ->
            val topicName = getTopicName(macAddress)
            try {
                mqttClient.send(topicName, recordEntity.toByteArray())
                    .also { it.waitForCompletion(transmissionTimeoutDuration.toLong(DurationUnit.MILLISECONDS)) }
                recordRepository.delete(recordEntity)
            } catch (mqttException: MqttException) {
                Log.e(loggerTag, "Could not send data with topic $topicName", mqttException)
                return Result.failure()
            }
        }

        return Result.success().also {
            Log.d(loggerTag, "Synchronization has successfully finished")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createNotificationChannel() {
        val channelName = "IoMT BLE Service"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun createNotification(): Notification {
        Log.d(loggerTag, "Creating notification...")
        val notificationIntent = Intent(context, EntryPointActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("IoMT synchronization")
            .setContentText("Synchronization is in progress...")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            .also { Log.d(loggerTag, "Notification created") }
    }

    companion object {
        private val loggerTag = MqttWorker::class.java.simpleName
        private const val NOTIFICATION_CHANNEL_ID = "mqtt-worker-notification"
        private const val NOTIFICATION_ID = 1002
        private val connectionTimeoutDuration = 1.minutes
        private val transmissionTimeoutDuration = 1.minutes
        private fun getTopicName(macAddress: String?): String = macAddress
            ?: throw IllegalArgumentException("Could not construct topic name: macAddress is null")
    }
}
