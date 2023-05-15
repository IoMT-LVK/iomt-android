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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import no.nordicsemi.android.ble.BleManager
import java.util.UUID

class BluetoothDeviceManager(
    private val config: DeviceConfig,
    private val deviceCharacteristicLink: DeviceCharacteristicLink,
    context: Context
) : BleManager(context)  {
    override fun getMinLogPriority(): Int = Log.VERBOSE

    override fun log(priority: Int, message: String) {
        Log.println(priority, loggerTag, message)
    }

    private val characteristics: MutableMap<String, BluetoothGattCharacteristic> = mutableMapOf()
    private val characteristicStates: MutableMap<String, MutableStateFlow<String>> = mutableMapOf()
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

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        return initializeCharacteristics(gatt).values
            .map { it.properties }
            .map { characteristicProperties ->
                (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ != 0) &&
                    (characteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
            }
            .all { it }

    }

    private val defaultScope = CoroutineScope(Dispatchers.Default)

    internal fun subscribeOn() = characteristicStates

    override fun initialize() {
        characteristics.map { (charName, characteristic) ->
            setNotificationCallback(characteristic).with { _, data ->
                if (data.value != null) {
                    val value = String(data.value!!, Charsets.UTF_8)
                    defaultScope.launch {
                        characteristicStates[charName]?.update { value }
                        deviceCharacteristicLink.let { link ->
                            val linkEntityId = link.getLinkIdByCharacteristicName(charName)
                            val record = RecordEntity(linkEntityId, LocalDateTime.now(), value)
                            recordRepository.insert(record)
                        }
                    }
                }
            }

            beginAtomicRequestQueue()
                .add(
                    enableNotifications(characteristic).fail { _: BluetoothDevice?, status: Int ->
                        log(Log.ERROR, "Could not subscribe: $status")
                        disconnect().enqueue()
                    }
                )
                .done { log(Log.INFO, "Target initialized") }
                .enqueue()
        }
    }

    override fun onServicesInvalidated() {
        characteristics.clear()
        characteristicStates.clear()
    }

    internal fun getDeviceWithConfigs(): BluetoothDeviceWithConfig? = bluetoothDevice?.let { BluetoothDeviceWithConfig(it, config) }
    internal fun getDevice(): BluetoothDevice? = bluetoothDevice

    companion object {
        private val loggerTag = BluetoothDeviceManager::class.java.simpleName
    }
}