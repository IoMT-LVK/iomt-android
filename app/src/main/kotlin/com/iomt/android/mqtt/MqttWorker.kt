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

import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException

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
    private val mqttClient = MqttClient()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        val recordsToSend = try {
            recordRepository.getNotSynchronized().map { recordEntity ->
                deviceCharacteristicLinkRepository
                    .getDeviceMacAndCharacteristicNameByLinkId(recordEntity.deviceCharacteristicLinkId)
                    ?.let { macAndCharName -> recordEntity to macAndCharName }
            }
        } catch (exception: IllegalArgumentException) {
            Log.e(loggerTag, "Error while fetching records from database", exception)
            return Result.failure()
        }
            .filterNotNull()

        try {
            mqttClient.connect()
        } catch (mqttException: Mqtt3ConnAckException) {
            Log.e(loggerTag, "Could not connect to MQTT broker", mqttException)
            return Result.failure()
        }

        val userId = inputData.getLong("userId", -1)
        require(userId != -1L) { "Could not get userId" }

        recordsToSend.map { (recordEntity, recordInfo) ->
            val (macAddress, characteristicName) = recordInfo
            val topic = Topic(userId, macAddress, characteristicName)
            try {
                mqttClient.send(topic, recordEntity.toMqttRecordMessage())
                recordRepository.update(recordEntity.apply { isSynchronized = true }.also { Log.d(loggerTag, Json.encodeToString(it)) })
            } catch (mqttException: RuntimeException) {
                Log.e(loggerTag, "Could not send data with topic ${topic.toTopicName()}", mqttException)
                return Result.failure()
            }
        }

        Log.d(loggerTag, "Synchronization has successfully finished")
        return Result.success().also { mqttClient.disconnect() }
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
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
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
    }
}
