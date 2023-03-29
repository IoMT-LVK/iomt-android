/**
 * Bluetooth LE scanner view
 */

package com.iomt.android.jetpack.view

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

/**
 * @param navController
 * @param bluetoothManager
 * @param onDeviceClick
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
@Composable
fun BleScannerView(navController: NavHostController, bluetoothManager: BluetoothManager, onDeviceClick: (BluetoothDevice) -> Unit) {
    val foundDevices = remember { mutableStateListOf<BluetoothDevice>() }
    val isScanning by remember { mutableStateOf(false) }

    val bluetoothAdapter = bluetoothManager.adapter
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    val connectedDevices: List<BluetoothDevice> = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)

    val leScanCallback = object : ScanCallback() {
        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device !in connectedDevices && device !in foundDevices) {
                foundDevices.add(device)
            }
        }
    }

    bluetoothLeScanner.startScan(leScanCallback)
    LaunchedEffect(isScanning) {
        if (isScanning) {
            delay(30.seconds)
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    Column(Modifier.fillMaxSize()) {
        foundDevices.map { device ->
            Row(
                Modifier.fillMaxWidth().clickable { onDeviceClick(device).also { navController.popBackStack() } },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(device.name)
                Spacer(Modifier.padding(10.dp))
                Text(device.address)
            }
        }
    }
}
