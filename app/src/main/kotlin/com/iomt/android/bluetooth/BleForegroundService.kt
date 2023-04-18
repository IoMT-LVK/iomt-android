package com.iomt.android.bluetooth

import android.Manifest
import android.app.*
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.iomt.android.EntryPointActivity
import com.iomt.android.R
import com.iomt.android.config.configs.DeviceConfig
import kotlinx.coroutines.*

/**
 * Foreground service that stores all the bluetooth data
 */
class BleForegroundService : Service() {
    private val binder = BleBinder()
    private lateinit var bleManager: BleManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate() {
        super.onCreate()
        bleManager = BleManager(applicationContext)
        Log.d(loggerTag, "BleForegroundService initialization has started")
        createNotificationChannel()
        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        Log.d(loggerTag, "BleForegroundService initialization has successfully finished")
    }

    override fun onBind(intent: Intent): IBinder = binder

    /**
     * @param bluetoothDevice [BluetoothDevice] to connect
     * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectDevice(bluetoothDevice: BluetoothDevice, deviceConfig: DeviceConfig) = runBlocking {
        bleManager.connectDevice(bluetoothDevice, deviceConfig).join()
        updateNotification()
    }

    /**
     * @param bluetoothDevice [BluetoothDevice] to disconnect
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnectDevice(bluetoothDevice: BluetoothDevice) = bleManager.disconnectDevice(bluetoothDevice.address)
        .also { updateNotification() }

    /**
     * Connected [BluetoothDevice]s as [List]
     */
    fun getConnectedDevices() = bleManager.getConnectedDevices().values.map { it.device }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createNotificationChannel() {
        val channelName = "IoMT BLE Service"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun createNotification(): Notification {
        Log.d(loggerTag, "Creating notification...")
        val notificationIntent = Intent(this, EntryPointActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )

        val notificationText = getNotificationText()

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("IoMT Health Center")
            .setContentText(notificationText)
            /* R.drawable.ic_notification */
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            .also { Log.d(loggerTag, "Notification created") }
    }

    /**
     * Callback to update notification text
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getNotificationText(): String = bleManager.getConnectedDevices()
        .values
        .joinToString("\n") { it.device.name }
        .ifBlank { "No connected devices" }

    /**
     * [Binder] implementation for [BleForegroundService]
     */
    inner class BleBinder : Binder() {
        /**
         * @return [BleForegroundService]
         */
        fun getService(): BleForegroundService = this@BleForegroundService
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "iomt_ble_service_channel"
        private const val NOTIFICATION_ID = 1001
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = object { }.javaClass.enclosingClass.simpleName
    }
}
