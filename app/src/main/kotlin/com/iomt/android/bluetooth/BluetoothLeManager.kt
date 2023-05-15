package com.iomt.android.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.room.devicechar.DeviceCharacteristicLink
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.room.record.RecordEntity
import com.iomt.android.room.record.RecordRepository
import com.iomt.android.utils.now
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime
import no.nordicsemi.android.ble.BleManager

/**
 * Key is characteristic name
 *
 * Value is [MutableStateFlow] of the value
 */
private typealias MutableDeviceStateFlow = MutableMap<String, MutableStateFlow<String>>

/**
 * Class that encapsulates all the BleGatt interactions
 */
class BluetoothLeManager(private val context: Context){
    private val deviceCharacteristicLinkRepository = DeviceCharacteristicLinkRepository(context)

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Key is MAC address of device
     * Value is [BluetoothDeviceManager] connected with it
     */
    private val bluetoothDeviceManagers: MutableMap<String, BluetoothDeviceManager> = mutableMapOf()

    /**
     * @return [Map] where keys are MAC addresses and values are [BluetoothGatt]s
     */
    fun getConnectedDevices(): Map<String, BluetoothDevice> = bluetoothDeviceManagers.mapNotNull { (mac, bluetoothDevice) ->
        bluetoothDevice.getDevice()?.let { mac to it }
    }.toMap()

    /**
     * @return [Map] where keys are MAC addresses and values are pairs: [BluetoothGatt] and [DeviceConfig]
     */
    fun getConnectedDevicesWithConfigs(): Map<String, BluetoothDeviceWithConfig> = bluetoothDeviceManagers.mapNotNull { (mac, bluetoothDevice) ->
        bluetoothDevice.getDeviceWithConfigs()?.let { mac to it }
    }.toMap()

    /**
     * TODO: WIP
     *     private fun findDevices() {
     *         val deviceFilter = BluetoothLeDeviceFilter.Builder()
     *             .setNamePattern(Pattern.compile("YourDeviceNamePattern"))
     *             .build()
     *
     *         val pairingRequest = AssociationRequest.Builder()
     *             .addDeviceFilter(deviceFilter)
     *             .setSingleDevice(false)
     *             .build()
     *
     *         val deviceManager = context.getSystemService(CompanionDeviceManager::class.java)
     *         deviceManager.associate(pairingRequest, object : CompanionDeviceManager.Callback() {
     *             @Deprecated("Deprecated in Java")
     *             override fun onDeviceFound(intentSender: IntentSender) {
     *                 super.onDeviceFound(intentSender)
     *                 context.startIntentSender(intentSender, null, 0, 0, 0)
     *             }
     *
     *             override fun onFailure(error: CharSequence?) {
     *                 // Handle failure
     *             }
     *         }, null)
     *     }
     */

    /**
     * TODO: WIP
     *     private fun reconnectToAssociatedDevices() {
     *         val deviceManager = context.getSystemService(CompanionDeviceManager::class.java)
     *         val macAddresses = if(Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
     *             deviceManager.myAssociations.map { it.deviceMacAddress?.toOuiString() }
     *         } else {
     *             deviceManager.associations
     *         }
     *
     *         macAddresses.map { macAddress ->
     *             connectedDevices.toList()
     *
     *                 .find { (mac, _) -> mac == macAddress }
     *                 ?.let { (_, gatt) -> connectToDevice(gatt.device) }
     *         }
     *     }
     *
     * @param device
     * @param deviceConfig
     * @return connection [Job]
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal fun connectDevice(device: BluetoothDevice, deviceConfig: DeviceConfig): Job = scope.launch {
        val macAddress = device.address
        if (bluetoothDeviceManagers.containsKey(macAddress)) { cancel("Device is already connected") }

        val deviceCharacteristicLink = requireNotNull(deviceCharacteristicLinkRepository.getLinkByDeviceMac(macAddress)) {
            "Could not find device with mac $macAddress in database"
        }

        try {
            bluetoothDeviceManagers[macAddress] = BluetoothDeviceManager(deviceConfig, deviceCharacteristicLink, context)
        } catch (exception: Throwable) {
            Log.e(loggerTag, exception.message.toString())
        }
    }

    /**
     * @param macAddress MAC address of Bluetooth LE device
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal fun disconnectDevice(macAddress: String) {
        bluetoothDeviceManagers.remove(macAddress)?.disconnect()?.await()
    }

    /**
     * @param deviceMac MAC address of Bluetooth LE device
     * @return [MutableDeviceStateFlow] corresponding to device with [deviceMac]
     */
    internal fun subscribeOn(deviceMac: String): MutableDeviceStateFlow = requireNotNull(bluetoothDeviceManagers[deviceMac]) {
        "No StateFlow created for $deviceMac"
    }.subscribeOn()

    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = BluetoothLeManager::class.java.simpleName
    }
}
