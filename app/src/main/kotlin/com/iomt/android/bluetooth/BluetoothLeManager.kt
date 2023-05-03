package com.iomt.android.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
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

typealias BluetoothGattWithConfig = Pair<BluetoothGatt, DeviceConfig>

/**
 * Key is characteristic name
 *
 * Value is [MutableStateFlow] of the value
 */
private typealias MutableDeviceStateFlow = MutableMap<String, MutableStateFlow<String>>

/**
 * Key is device MAC address
 *
 * Value is [DeviceStateFlow]
 */
private typealias DeviceStateFlowMap = MutableMap<String, MutableDeviceStateFlow>

/**
 * Key is device MAC
 *
 * Value is [BluetoothGatt] - connected device with corresponding MAC address
 */
private typealias ConnectedDevicesMap = MutableMap<String, BluetoothGatt>

/**
 * Class that encapsulates all the BleGatt interactions
 */
class BluetoothLeManager(private val context: Context) {
    /**
     * @property config config of device
     * @property deviceCharacteristicLink device-related data from database
     */
    data class DeviceAdditionalData(
        val config: DeviceConfig,
        val deviceCharacteristicLink: DeviceCharacteristicLink,
    )

    // Key is MAC address of device
    // Value is [BluetoothGatt] connected with it
    private val connectedDevices: ConnectedDevicesMap = mutableMapOf()
    private val additionalData: MutableMap<String, DeviceAdditionalData> = mutableMapOf()
    private val stateFlows: DeviceStateFlowMap = mutableMapOf()

    private val deviceCharacteristicLinkRepository = DeviceCharacteristicLinkRepository(context)
    private val recordRepository = RecordRepository(context)

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * @return [Map] where keys are MAC addresses and values are [BluetoothGatt]s
     */
    fun getConnectedDevices(): Map<String, BluetoothGatt> = connectedDevices

    /**
     * @return [Map] where keys are MAC addresses and values are pairs: [BluetoothGatt] and [DeviceConfig]
     */
    fun getConnectedDevicesWithConfigs(): Map<String, BluetoothGattWithConfig> = connectedDevices.map { (macAddress, bluetoothGatt) ->
        macAddress to (bluetoothGatt to additionalData.getValue(macAddress).config)
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
        if (connectedDevices.containsKey(macAddress)) {
            cancel("Device is already connected")
        }

        val deviceCharacteristicLink = requireNotNull(deviceCharacteristicLinkRepository.getLinkByDeviceMac(macAddress)) {
            "Could not find device with mac $macAddress in database"
        }

        try {
            val deviceStateFlow: MutableDeviceStateFlow = mutableMapOf()
            deviceConfig.characteristics.keys.map { deviceStateFlow[it] = MutableStateFlow("- -") }
            additionalData[macAddress] = DeviceAdditionalData(deviceConfig, deviceCharacteristicLink)
            val bleGattCallback = getBleGattCallbackForDevice(macAddress, deviceStateFlow)
            val gatt = device.connectGatt(context, true, bleGattCallback)
            connectedDevices[macAddress] = gatt
            stateFlows[macAddress] = deviceStateFlow
        } catch (exception: Throwable) {
            Log.e(loggerTag, exception.message.toString())
            try {
                additionalData.remove(macAddress)
            } finally {
                stateFlows.remove(macAddress)
            }
        }
    }

    /**
     * @param macAddress MAC address of Bluetooth LE device
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal fun disconnectDevice(macAddress: String) {
        connectedDevices.remove(macAddress)?.close()
        additionalData.remove(macAddress)
    }

    /**
     * @param deviceMac MAC address of Bluetooth LE device
     * @return [MutableDeviceStateFlow] corresponding to device with [deviceMac]
     */
    internal fun subscribeOn(deviceMac: String): MutableDeviceStateFlow = requireNotNull(stateFlows[deviceMac]) {
        "No StateFlow created for $deviceMac"
    }

    private fun getBleGattCallbackForDevice(macAddress: String, deviceStateFlow: MutableDeviceStateFlow): BluetoothLeGattCallback {
        val deviceData = requireNotNull(additionalData[macAddress]) {
            "Could not find device with mac address $macAddress"
        }
        return BluetoothLeGattCallback(deviceData.config) { charName, newValue ->
            scope.launch {
                deviceData.deviceCharacteristicLink.let { link ->
                    val linkEntityId = link.getLinkIdByCharacteristicName(charName)
                    val record = RecordEntity(linkEntityId, LocalDateTime.now(), newValue)
                    recordRepository.insert(record)
                }
            }
            deviceStateFlow[charName]?.let { stateFlow -> stateFlow.update { newValue } }
        }
    }
    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = BluetoothLeManager::class.java.simpleName
    }
}
