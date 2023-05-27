package com.iomt.android.bluetooth

import android.app.*
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.iomt.android.configs.DeviceConfig

/**
 * Service for old android versions that stores all the bluetooth data
 */
class BluetoothLeLegacyService : Service(), BluetoothLeService {
    private val binder = BluetoothLeBinder()
    private lateinit var bluetoothLeManager: BluetoothLeManager

    override fun onCreate() {
        super.onCreate()
        bluetoothLeManager = BluetoothLeManager(applicationContext)
        Log.d(loggerTag, "BluetoothLeForegroundService initialization has started")

        /* Create service */

        Log.d(loggerTag, "BluetoothLeForegroundService initialization has successfully finished")
    }

    override fun onBind(intent: Intent): IBinder = binder

    /**
     * @param bluetoothDevice [BluetoothDevice] to connect
     * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
     */
    override suspend fun connectDevice(bluetoothDevice: BluetoothDevice, deviceConfig: DeviceConfig) {
        bluetoothLeManager.connectDevice(bluetoothDevice, deviceConfig)
    }

    /**
     * @param bluetoothDevice [BluetoothDevice] to disconnect
     */
    override suspend fun disconnectDevice(bluetoothDevice: BluetoothDevice) = bluetoothLeManager.disconnectDevice(
        bluetoothDevice.address,
    ) ?: Unit

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_STICKY
    }

    /**
     * [Binder] implementation for [BluetoothLeLegacyService]
     */
    inner class BluetoothLeBinder : Binder() {
        /**
         * @return [BluetoothLeLegacyService]
         */
        fun getService(): BluetoothLeLegacyService = this@BluetoothLeLegacyService
    }

    companion object {
        private val loggerTag = BluetoothLeLegacyService::class.java.simpleName
    }
}
