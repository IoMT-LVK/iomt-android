package com.iomt.android.bluetooth

import android.bluetooth.BluetoothDevice
import com.iomt.android.configs.DeviceConfig

/**
 * Interface that should be implemented by both [BluetoothLeLegacyService] and [BluetoothLeForegroundService]
 */
interface BluetoothLeService {
    /**
     * @param bluetoothDevice [BluetoothDevice] to connect
     * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
     */
    suspend fun connectDevice(bluetoothDevice: BluetoothDevice, deviceConfig: DeviceConfig)

    /**
     * @param bluetoothDevice [BluetoothDevice] to disconnect
     */
    suspend fun disconnectDevice(bluetoothDevice: BluetoothDevice)

    /**
     * Method to subscribe on Device's characteristics updates
     *
     * @param deviceMac MAC address of Bluetooth LE device
     * @return [DeviceStateFlow] corresponding to Bluetooth LE device with [deviceMac]
     */
    fun subscribeOn(deviceMac: String): DeviceStateFlow

    /**
     * @return connected [BluetoothDevice]s as [Collection]
     */
    fun getConnectedDevices(): Collection<BluetoothDevice>

    /**
     * @param macAddress MAC address of Bluetooth LE device
     * @return connected [BluetoothDevice] by its [macAddress]
     */
    fun getConnectedDevice(macAddress: String): BluetoothDevice?

    /**
     * @return [List] of pairs: connected [BluetoothDevice]s and their [DeviceConfig]s
     */
    fun getConnectedDevicesWithConfigs(): Collection<BluetoothDeviceWithConfig>
}
