package com.iomt.android.dbcleaner

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
import com.iomt.android.room.record.RecordRepository
import com.iomt.android.utils.beforeNow

import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.*

/**
 * Class that implements [CoroutineWorker] in order to define record data cleanup
 */
class CleanerWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    private val recordRepository = RecordRepository(context)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())

        val ttlDuration = inputData.getLong("dataTtlMinutes", 1440).minutes

        val localDateTime = LocalDateTime.beforeNow(ttlDuration)
        Log.d(loggerTag, "Deleting synchronized records older then $ttlDuration")
        recordRepository.cleanSynchronizedRecordsOlderThen(localDateTime)
        Log.d(loggerTag, "Cleanup has successfully finished")
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createNotificationChannel() {
        val channelName = "IoMT cleaner service"
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
            .setContentTitle("IoMT cleaner")
            .setContentText("Cleanup is in progress...")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            .also { Log.d(loggerTag, "Notification created") }
    }

    companion object {
        private val loggerTag = CleanerWorker::class.java.simpleName
        private const val NOTIFICATION_CHANNEL_ID = "cleaner-worker-notification"
        private const val NOTIFICATION_ID = 1003
    }
}
