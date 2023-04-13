/**
 * Home View
 */

package com.iomt.android.jetpack.view.main

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.iomt.android.jetpack.components.DeviceList
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.utils.rememberBoundService
import com.iomt.android.utils.withLoading

/**
 * @param onKnownDeviceClick callback invoked on known device click
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun HomeView(onKnownDeviceClick: (BluetoothDevice) -> Unit) {
    val bleForegroundService by rememberBoundService().collectAsState()
    withLoading(bleForegroundService) { foregroundService ->
        val knownDevices = remember { foregroundService.getConnectedDevices().toMutableStateList() }
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            DeviceList("Known devices", knownDevices) { onKnownDeviceClick(it) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Preview
@Composable
private fun HomeViewPreview() {
    val connectedDevices = remember { mutableStateListOf<BluetoothDevice>() }
    MaterialTheme(colorScheme) { HomeView { connectedDevices.add(it) } }
}
