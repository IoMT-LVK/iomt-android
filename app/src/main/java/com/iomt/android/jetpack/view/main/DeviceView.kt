/**
 * Device View
 */

package com.iomt.android.jetpack.view.main

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.utils.getService

/**
 * @param device
 * @param config
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceView(device: BluetoothDevice, config: DeviceConfig) {
    var isConnected by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        DeviceTitleCard(device, isConnected)
        Spacer(Modifier.padding(20.dp))
        CharacteristicsSection(config)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
private fun DeviceTitleCard(bluetoothDevice: BluetoothDevice, isConnected: Boolean) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(100.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Icon(painterResource(R.drawable.default_device), "Icon", Modifier.size(50.dp))
            Column(Modifier.fillMaxWidth().weight(1f)) {
                Text(bluetoothDevice.name, fontSize = TextUnit(6f, TextUnitType.Em))
                if (isConnected) {
                    Text("Connected", fontWeight = FontWeight.Light)
                } else {
                    Text("Disconnected", fontWeight = FontWeight.Light)
                }
            }
            StatusIcon(isConnected)
        }
    }
}

@Composable
private fun StatusIcon(isConnected: Boolean) {
    if (isConnected) {
        Icon(painterResource(R.drawable.blt), "Connected", tint = Color.Blue)
    } else {
        Icon(painterResource(R.drawable.nosig), "Disconnected", tint = Color.Red)
    }
}

@Composable
private fun CharacteristicsSection(config: DeviceConfig) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        config.characteristics.map { (charName, charConfig) ->
            Icon(painterResource(R.drawable.lungs), charName)
            Text(charName)
            Text(0.toString())
        }
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
