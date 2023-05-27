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
import com.iomt.android.configs.DeviceConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * Key is characteristic name
 *
 * Value is [StateFlow] of the value
 */
typealias DeviceStateFlow = Map<String, StateFlow<String>>

/**
 * Foreground service that stores all the bluetooth data
 */
@RequiresApi(Build.VERSION_CODES.S)
class BluetoothLeForegroundService : Service(), BluetoothLeService {
    private val binder = BluetoothLeBinder()
    private lateinit var bluetoothLeManager: BluetoothLeManager

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate() {
        super.onCreate()
        bluetoothLeManager = BluetoothLeManager(applicationContext)
        Log.d(loggerTag, "BluetoothLeForegroundService initialization has started")
        createNotificationChannel()
        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        Log.d(loggerTag, "BluetoothLeForegroundService initialization has successfully finished")
    }

    override fun onBind(intent: Intent): IBinder = binder

    /**
     * @param bluetoothDevice [BluetoothDevice] to connect
     * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun connectDevice(bluetoothDevice: BluetoothDevice, deviceConfig: DeviceConfig) {
        bluetoothLeManager.connectDevice(bluetoothDevice, deviceConfig)
        updateNotification()
    }

    /**
     * @param bluetoothDevice [BluetoothDevice] to disconnect
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun disconnectDevice(bluetoothDevice: BluetoothDevice) = bluetoothLeManager.disconnectDevice(bluetoothDevice.address)
        .also { updateNotification() } ?: Unit

    /**
     * Method to subscribe on Device's characteristics updates
     *
     * @param deviceMac MAC address of Bluetooth LE device
     * @return [DeviceStateFlow] corresponding to Bluetooth LE device with [deviceMac]
     */
    override fun subscribeOn(deviceMac: String): DeviceStateFlow = bluetoothLeManager.subscribeOn(deviceMac)

    /**
     * @return connected [BluetoothDevice]s as [List]
     */
    override fun getConnectedDevices() = bluetoothLeManager.getConnectedDevices().values

    /**
     * @param macAddress MAC address of Bluetooth LE device
     * @return connected [BluetoothDevice] by its [macAddress]
     */
    override fun getConnectedDevice(macAddress: String) = bluetoothLeManager.getConnectedDevices()[macAddress]

    /**
     * @return [List] of pairs: connected [BluetoothDevice]s and their [DeviceConfig]s
     */
    override fun getConnectedDevicesWithConfigs() = bluetoothLeManager.getConnectedDevicesWithConfigs().values

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channelName = "IoMT BLE Service"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
            .also { Log.d(loggerTag, "Notification created") }
    }

    /**
     * Callback to update notification text
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getNotificationText(): String = bluetoothLeManager.getConnectedDevices()
        .values
        .joinToString("\n") { it.name }
        .ifBlank { "No connected devices" }

    /**
     * [Binder] implementation for [BluetoothLeForegroundService]
     */
    inner class BluetoothLeBinder : Binder() {
        /**
         * @return [BluetoothLeForegroundService]
         */
        fun getService(): BluetoothLeForegroundService = this@BluetoothLeForegroundService
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "iomt_ble_service_channel"
        private const val NOTIFICATION_ID = 1001
        private val loggerTag = BluetoothLeForegroundService::class.java.simpleName
    }
}
