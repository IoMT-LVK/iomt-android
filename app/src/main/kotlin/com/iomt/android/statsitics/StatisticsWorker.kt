package com.iomt.android.statsitics

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
import com.iomt.android.room.statistics.StatisticsRepository

/**
 * Class that implements [CoroutineWorker] in order to define record database statistics
 */
class StatisticsWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    private val recordRepository = RecordRepository(context)
    private val statisticsRepository = StatisticsRepository(context)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())

        val statistics = recordRepository.collectStatistics()
        statisticsRepository.insertAll(statistics)

        Log.d(loggerTag, "Saved ${statistics.count()} statistics entities")
        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createNotificationChannel() {
        val channelName = "IoMT statistics service"
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
            .setContentTitle("IoMT statistics")
            .setContentText("Statistics recording is in progress...")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            .also { Log.d(loggerTag, "Notification created") }
    }

    companion object {
        private val loggerTag = StatisticsWorker::class.java.simpleName
        private const val NOTIFICATION_CHANNEL_ID = "statistics-worker-notification"
        private const val NOTIFICATION_ID = 1004
    }
}
