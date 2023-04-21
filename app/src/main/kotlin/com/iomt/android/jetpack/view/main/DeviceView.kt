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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iomt.android.bluetooth.ConnectionStatus
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.configs.toCharacteristics
import com.iomt.android.entities.Characteristic
import com.iomt.android.jetpack.components.device.DeviceBodyCard
import com.iomt.android.jetpack.components.device.DeviceHeaderCard
import com.iomt.android.utils.rememberBoundService
import com.iomt.android.utils.withLoading

/**
 * @param macAddress MAC address of [BluetoothDevice]
 * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceView(macAddress: String, deviceConfig: DeviceConfig) {
    val characteristics = remember { mutableStateListOf<Characteristic>().apply { addAll(deviceConfig.characteristics.toCharacteristics()) } }
    val bleForegroundService by rememberBoundService().collectAsState()
    withLoading(bleForegroundService) { foregroundService ->
        val device by remember { mutableStateOf(foregroundService.getConnectedDevices().find { it.address == macAddress }!!) }
        var connectionStatus by remember { mutableStateOf(ConnectionStatus.fromBondState(device.bondState)) }
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            DeviceHeaderCard(device, connectionStatus) {
                when (connectionStatus) {
                    ConnectionStatus.DISCONNECTED -> bleForegroundService?.connectDevice(device, deviceConfig).also { connectionStatus = ConnectionStatus.CONNECTED }
                    ConnectionStatus.CONNECTING -> {
                        Log.d("GattStatusChangeRequest", "None due to CONNECTING state")
                        Unit
                    }
                    ConnectionStatus.CONNECTED -> {
                        Log.d("GattStatusChangeRequest", "Request to disconnect")
                        bleForegroundService?.disconnectDevice(device).also { connectionStatus = ConnectionStatus.DISCONNECTED }
                    }
                }
            }
            Spacer(Modifier.padding(20.dp))
            DeviceBodyCard(characteristics)
        }
    }
}
