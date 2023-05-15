package com.iomt.android.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log

import com.iomt.android.configs.CharacteristicConfig
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.room.devicechar.DeviceCharacteristicLink
import com.iomt.android.room.record.RecordEntity
import com.iomt.android.room.record.RecordRepository
import com.iomt.android.utils.now

import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data.FORMAT_UINT16
import no.nordicsemi.android.ble.data.Data.FORMAT_UINT8

import java.util.UUID

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * Key is characteristic name
 *
 * Value is [BluetoothGattCharacteristic] of the value
 */
typealias CharacteristicMap = MutableMap<String, BluetoothGattCharacteristic>

/**
 * Key is characteristic name
 *
 * Value is [MutableStateFlow] of the value
 */
typealias CharacteristicStateFlowMap = MutableMap<String, MutableStateFlow<String>>

/**
 * Class that encapsulates all bluetooth interaction with one device.
 */
class BluetoothDeviceManager(
    private val config: DeviceConfig,
    private val deviceCharacteristicLink: DeviceCharacteristicLink,
    context: Context,
) : BleManager(context) {
    private val defaultScope = CoroutineScope(Dispatchers.Default)
    private val characteristics: CharacteristicMap = mutableMapOf()
    private val characteristicStates: CharacteristicStateFlowMap = mutableMapOf()
    private val recordRepository = RecordRepository(context)
    private fun getGattCharacteristic(gatt: BluetoothGatt, characteristicConfig: CharacteristicConfig): BluetoothGattCharacteristic? {
        val svcUuid = characteristicConfig.serviceUuid?.let { serviceUuid -> UUID.fromString(serviceUuid) }
        val charUuid = characteristicConfig.characteristicUuid?.let { characteristicUuid -> UUID.fromString(characteristicUuid) }
        return svcUuid?.let { service -> charUuid?.let { characteristic -> service to characteristic } }
            ?.let { (serviceUuid, characteristicUuid) -> gatt.getService(serviceUuid)?.getCharacteristic(characteristicUuid) }
    }

    private fun initializeCharacteristics(gatt: BluetoothGatt) = config.characteristics
        .mapNotNull { (charName, value) -> getGattCharacteristic(gatt, value)?.let { charName to it } }
        .toMap()
        .also { charMap -> charMap.keys.map { charName -> characteristicStates[charName] = MutableStateFlow("- -") } }
        .also { characteristics.putAll(it) }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean = initializeCharacteristics(gatt).values
        .map { it.properties }
        .map { characteristicProperties ->
            (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ != 0) &&
                    (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
        }
        .all { it }

    override fun shouldClearCacheWhenDisconnected(): Boolean = true

    override fun getServiceDiscoveryDelay(bonded: Boolean): Int = if (bonded) super.getServiceDiscoveryDelay(true) else 2000

    override fun initialize() {
        characteristics.map { (charName, characteristic) ->
            setNotificationCallback(characteristic).with { _, data ->
                data.value?.let { values ->
                    val isUint8 = values[0].toInt().and(0x01).let { it == 0 }
                    val value = data.getIntValue(if (isUint8) FORMAT_UINT8 else @Suppress("DEPRECATION") FORMAT_UINT16, 1)
                    defaultScope.launch {
                        characteristicStates[charName]?.update { value.toString() }
                        deviceCharacteristicLink.let { link ->
                            val linkEntityId = link.getLinkIdByCharacteristicName(charName)
                            val record = RecordEntity(linkEntityId, LocalDateTime.now(), value.toString())
                            recordRepository.insert(record)
                        }
                    }
                }
            }

            beginAtomicRequestQueue().add(
                enableNotifications(characteristic).fail { _: BluetoothDevice?, status: Int ->
                    log(Log.ERROR, "Could not subscribe: $status")
                    disconnect().enqueue()
                },
            )
                .done { log(Log.INFO, "Target initialized") }
                .enqueue()
        }
    }

    override fun onServicesInvalidated() {
        characteristics.clear()
        characteristicStates.clear()
    }

    override fun getMinLogPriority(): Int = Log.DEBUG

    override fun log(priority: Int, message: String) {
        Log.println(priority, loggerTag, message)
    }

    /**
     * @return [BluetoothDeviceWithConfig] corresponding device managed by [BluetoothDeviceManager]
     */
    internal fun getDeviceWithConfigs(): BluetoothDeviceWithConfig? = bluetoothDevice?.let { BluetoothDeviceWithConfig(it, config) }

    /**
     * @return [BluetoothDevice] corresponding device managed by [BluetoothDeviceManager]
     */
    internal fun getDevice(): BluetoothDevice? = bluetoothDevice

    /**
     * @return [characteristicStates], where key is characteristic name and value is [MutableStateFlow] - current value
     */
    internal fun subscribeOn() = characteristicStates

    companion object {
        private val loggerTag = BluetoothDeviceManager::class.java.simpleName
    }
}
