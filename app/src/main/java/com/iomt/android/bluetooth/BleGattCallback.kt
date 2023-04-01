package com.iomt.android.bluetooth

import android.Manifest
import android.bluetooth.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.snapshots.SnapshotStateList

import com.iomt.android.config.configs.CharacteristicConfig
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.entities.Characteristic

import java.util.*

import kotlin.experimental.and
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Class that implements [BluetoothGattCallback]
 */
class BleGattCallback(
    private val deviceConfig: DeviceConfig,
    private val characteristics: SnapshotStateList<Characteristic>,
    private val changeStatus: (Int) -> ConnectionStatus
) : BluetoothGattCallback() {
    private fun updateCharacteristic(charName: String, newValue: String) {
        val index = characteristics.indexOfFirst { it.name == charName }
        val oldItem = characteristics[index]
        characteristics[index] = oldItem.copy(value = newValue)
    }

    /**
     * Method invoked when Bluetooth connection state changes
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        MainScope().launch {
            val newConnectionStatus = changeStatus(newState)
            Log.d("BleGattCallback", "Changed state to $newConnectionStatus")
        }
    }

    /**
     * Initialize characteristic in order to fetch data from it
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun initializeCharacteristic(
        gatt: BluetoothGatt,
        characteristicConfig: CharacteristicConfig,
    ) {
        val service = gatt.getService(UUID.fromString(characteristicConfig.serviceUuid))
        service.getCharacteristic(UUID.fromString(characteristicConfig.characteristicUuid)).also { characteristic ->
            gatt.setCharacteristicNotification(characteristic, true)
            characteristic.getDescriptor(clientCharacteristicConfigUuid).let {
                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(it)
            }
        }
    }

    /**
     * Initialize all the characteristics mentioned in config file
     *
     * @param gatt [BluetoothGatt]
     * @param status [BluetoothGatt.GATT_SUCCESS] or [BluetoothGatt.GATT_FAILURE]
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("BleGattCallback", "Successfully discovered services")
            deviceConfig.characteristics.map { (charName, config) ->
                characteristics.find { it.name == charName }?.apply {
                    initializeCharacteristic(gatt, config)
                }
            }
        } else {
            Log.d("BleGattCallback", "Failed to discover services")
        }
    }

    private fun changeCharacteristicLabel(
        characteristicName: String,
        gattCharacteristic: BluetoothGattCharacteristic,
    ) {
        val isUint8 = gattCharacteristic.value[0].toInt().and(0x01).let { it == 0 }
        val format: Int = if (isUint8) {
            BluetoothGattCharacteristic.FORMAT_UINT8
        } else {
            BluetoothGattCharacteristic.FORMAT_UINT16
        }
        val value = gattCharacteristic.getIntValue(format, 1)
        updateCharacteristic(characteristicName, value.toString())
        Log.i(characteristicName, value.toString())
    }

    private fun changeAccelerometerLabel(accelerometerCharacteristic: BluetoothGattCharacteristic) {
        val data = accelerometerCharacteristic.value
        val flag = data[0]
        val format = BluetoothGattCharacteristic.FORMAT_UINT16
        var dataIndex = 1
        val isStepCountPresent = flag and 0x01 != 0.toByte()
        val isActivityPresent = flag and 0x02 != 0.toByte()
        val isCadencePresent = flag and 0x04 != 0.toByte()
        if (isStepCountPresent) {
            val stepCount = accelerometerCharacteristic.getIntValue(format, dataIndex)
            updateCharacteristic("stepCound", stepCount.toString())
            dataIndex += 2
        }
        if (isActivityPresent) {
            val activity = accelerometerCharacteristic.getIntValue(format, dataIndex) / 256.0f
            updateCharacteristic("activityRate", activity.toString())
            dataIndex += 2
        }
        if (isCadencePresent) {
            val cadence = accelerometerCharacteristic.getIntValue(format, dataIndex)
            updateCharacteristic("cadence", cadence.toString())
        }
    }

    @Suppress("COMMENTED_OUT_CODE")
    @Deprecated("Deprecated in Java")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.d("BluetoothGattCharacteristicChanged", characteristic.uuid.toString())
        deviceConfig.characteristics.filter { (_, config) ->
            UUID.fromString(config.characteristicUuid) == characteristic.uuid
        }
            .map { it.key }
            .firstOrNull()
            ?.let {
                if (it == "accelerometer") {
                    changeAccelerometerLabel(characteristic)
                } else {
                    changeCharacteristicLabel(it, characteristic)
                }
            }

        // TODO: WIP
        // val dfDateAndTime: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        // val milliseconds: DateFormat = SimpleDateFormat("SSS", Locale.US)
        // val now = Date()
        // val myDate = dfDateAndTime.format(now)
        // val millis = milliseconds.format(now)
        // val result = JSONObject().apply {
        // put("Clitime", myDate)
        // put("Millisec", Integer.valueOf(millis))
        // }
        // characteristics.filter { (_, characteristic) ->
        // characteristic.isUpdated
        // }.forEach { (charName, characteristic) ->
        // result.put(charName, Integer.valueOf(characteristic.textView.text.toString()))
        // }
    }
    companion object {
        private val clientCharacteristicConfigUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}
