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
import kotlinx.datetime.LocalDateTime

/**
 * Class that encapsulates all the BleGatt interactions
 */
class BleManager(private val context: Context) {
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
    private val connectedDevices: MutableMap<String, BluetoothGatt> = mutableMapOf()
    private val additionalData: MutableMap<String, DeviceAdditionalData> = mutableMapOf()

    private val deviceCharacteristicLinkRepository = DeviceCharacteristicLinkRepository(context)
    private val recordRepository = RecordRepository(context)

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * @return [connectedDevices]
     */
    fun getConnectedDevices(): Map<String, BluetoothGatt> = connectedDevices

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
            additionalData[macAddress] = DeviceAdditionalData(deviceConfig, deviceCharacteristicLink)
            val bleGattCallback = getBleGattCallbackForDevice(macAddress)
            val gatt = device.connectGatt(context, true, bleGattCallback)
            connectedDevices[macAddress] = gatt
        } catch (exception: Throwable) {
            Log.e(loggerTag, exception.message.toString())
            additionalData.remove(macAddress)
        }
    }

    /**
     * @param macAddress
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal fun disconnectDevice(macAddress: String) {
        connectedDevices.remove(macAddress)?.close()
        additionalData.remove(macAddress)
    }

    private fun getBleGattCallbackForDevice(macAddress: String): BleGattCallback {
        val deviceData = requireNotNull(additionalData[macAddress]) {
            "Could not find device with mac address $macAddress"
        }
        return BleGattCallback(deviceData.config) { charName, newValue ->
            scope.launch {
                deviceData.deviceCharacteristicLink.let { link ->
                    Log.e(loggerTag, link.toString())
                    val linkEntityId = link.getLinkIdByCharacteristicName(charName)
                    val record = RecordEntity(linkEntityId, LocalDateTime.now(), newValue)
                    recordRepository.insert(record)
                }
            }
        }
    }
    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = object { }.javaClass.enclosingClass.simpleName
    }
}
