/**
 * Device View
 */

package com.iomt.android.jetpack.view.main

import android.Manifest
import android.bluetooth.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.bluetooth.BleGattCallback
import com.iomt.android.bluetooth.ConnectionStatus
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.config.configs.toCharacteristics
import com.iomt.android.entities.Characteristic
import com.iomt.android.jetpack.components.device.DeviceBodyCard
import com.iomt.android.jetpack.components.device.DeviceHeaderCard
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.room.device.DeviceEntity
import com.iomt.android.room.device.DeviceRepository
import com.iomt.android.room.devicechar.DeviceCharacteristicLink
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.room.record.RecordEntity
import com.iomt.android.room.record.RecordRepository
import com.iomt.android.utils.getService
import com.iomt.android.utils.now
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * @param device [BluetoothDevice] that this view is created for
 * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceView(device: BluetoothDevice, deviceConfig: DeviceConfig) {
    val context = LocalContext.current
    val deviceRepository = DeviceRepository(context)
    var deviceEntity by remember { mutableStateOf<DeviceEntity?>(null) }
    val linkRepository = DeviceCharacteristicLinkRepository(context)
    var link by remember { mutableStateOf<DeviceCharacteristicLink?>(null) }
    LaunchedEffect(device) {
        val fetchedDevice = deviceRepository.getByMacOrSave(device.address, device.name)
        deviceEntity = fetchedDevice
        link = linkRepository.getLinksByDeviceId(fetchedDevice.id!!)
    }

    var bluetoothGatt by remember { mutableStateOf<BluetoothGatt?>(null) }
    var connectionStatus by remember { mutableStateOf(ConnectionStatus.DISCONNECTED) }
    val changeStateCallback: (Int) -> ConnectionStatus = { newState ->
        when (newState) {
            BluetoothProfile.STATE_DISCONNECTED -> connectionStatus = ConnectionStatus.DISCONNECTED
            BluetoothProfile.STATE_CONNECTING, BluetoothProfile.STATE_DISCONNECTING -> connectionStatus = ConnectionStatus.CONNECTING
            BluetoothProfile.STATE_CONNECTED -> {
                connectionStatus = ConnectionStatus.CONNECTED
                val isStarted = bluetoothGatt?.discoverServices()
                Log.d("DiscoverService", "Started: $isStarted")
            }
            else -> { }
        }
        connectionStatus
    }

    val scope = rememberCoroutineScope()
    val recordRepository = RecordRepository(context)
    val characteristics = remember { mutableStateListOf<Characteristic>().apply { addAll(deviceConfig.characteristics.toCharacteristics()) } }
    val bleGattCallback = BleGattCallback(deviceConfig, characteristics, changeStateCallback) { charName, newValue ->
        scope.launch {
            link?.let { link ->
                val linkEntityId = link.getLinkIdByCharacteristicName(charName)
                val record = RecordEntity(linkEntityId, LocalDateTime.now(), newValue)
                /* sendRecordToServer(record) */
                recordRepository.insert(record)
            }
        }
    }

    bluetoothGatt ?: run { bluetoothGatt = device.connectGatt(context, true, bleGattCallback) }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        DeviceHeaderCard(device, connectionStatus) {
            when (connectionStatus) {
                ConnectionStatus.DISCONNECTED -> bluetoothGatt?.let { gatt ->
                    val isSuccessful = gatt.connect()
                    Log.d("GattStatusChangeRequest", "Request to connect: isSuccessful = $isSuccessful")
                } ?: run {
                    Log.d("GattStatusChangeRequest", "Request to disconnect")
                    bluetoothGatt = device.connectGatt(context, true, bleGattCallback).apply { connect() }
                }
                ConnectionStatus.CONNECTING -> {
                    Log.d("GattStatusChangeRequest", "None due to CONNECTING state")
                    Unit
                }
                ConnectionStatus.CONNECTED -> {
                    Log.d("GattStatusChangeRequest", "Request to disconnect")
                    bluetoothGatt?.disconnect()
                }
            }
        }
        Spacer(Modifier.padding(20.dp))
        DeviceBodyCard(characteristics)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Preview
@Composable
private fun DeviceViewPreview() {
    val stubDevice = LocalContext.getService<BluetoothManager>().adapter.getRemoteDevice("test-address")
    MaterialTheme(colorScheme) { DeviceView(stubDevice, DeviceConfig.stub) }
}
