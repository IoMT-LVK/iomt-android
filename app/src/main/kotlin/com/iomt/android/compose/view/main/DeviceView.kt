/**
 * Device View
 */

package com.iomt.android.compose.view.main

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
import com.iomt.android.compose.components.device.DeviceBodyCard
import com.iomt.android.compose.components.device.DeviceHeaderCard
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.entities.Characteristic
import com.iomt.android.utils.rememberBoundService
import com.iomt.android.utils.withLoading
import kotlinx.coroutines.launch

/**
 * @param macAddress MAC address of [BluetoothDevice]
 * @param deviceConfig [DeviceConfig] of [BluetoothDevice]
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceView(macAddress: String, deviceConfig: DeviceConfig) {
    val bluetoothLeForegroundService by rememberBoundService().collectAsState()
    withLoading(bluetoothLeForegroundService) { foregroundService ->
        val device by remember { mutableStateOf(foregroundService.getConnectedDevice(macAddress)!!) }
        val characteristicStates by remember { mutableStateOf(foregroundService.subscribeOn(macAddress)) }
        val characteristics = characteristicStates.map { (name, stateFlow) ->
            Characteristic(name, deviceConfig.characteristics[name]!!.prettyName, stateFlow)
        }
        val scope = rememberCoroutineScope()
        var connectionStatus by remember { mutableStateOf(ConnectionStatus.CONNECTED) }
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            DeviceHeaderCard(device, connectionStatus) {
                when (connectionStatus) {
                    ConnectionStatus.DISCONNECTED -> scope.launch { bluetoothLeForegroundService?.connectDevice(device, deviceConfig) }
                        .also { connectionStatus = ConnectionStatus.CONNECTED }
                    ConnectionStatus.CONNECTING -> {
                        Log.d("GattStatusChangeRequest", "None due to CONNECTING state")
                        Unit
                    }
                    ConnectionStatus.CONNECTED -> {
                        Log.d("GattStatusChangeRequest", "Request to disconnect")
                        scope.launch { bluetoothLeForegroundService?.disconnectDevice(device).also { connectionStatus = ConnectionStatus.DISCONNECTED } }
                    }
                }
            }
            Spacer(Modifier.padding(20.dp))
            DeviceBodyCard(macAddress, characteristics)
        }
    }
}
