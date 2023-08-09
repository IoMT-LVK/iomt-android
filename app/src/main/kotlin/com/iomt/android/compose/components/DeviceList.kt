/**
 * Component for device list rendering
 */

package com.iomt.android.compose.components

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iomt.android.bluetooth.BluetoothDeviceWithConfig

/**
 * @param title label for [DeviceList]
 * @param deviceWithConfigList [List] of [BluetoothDeviceWithConfig]s
 * @param onItemClicked callback invoked on Device item click
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceList(
    title: String,
    deviceWithConfigList: List<BluetoothDeviceWithConfig>,
    onItemClicked: (BluetoothDeviceWithConfig) -> Unit,
) {
    OutlinedCard(Modifier.fillMaxWidth().defaultMinSize(minHeight = 25.dp)) {
        if (deviceWithConfigList.isEmpty()) {
            Text("No ${title.lowercase()}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        } else {
            Row { Text(title) }
            deviceWithConfigList.map { deviceWithConfig ->
                Row(
                    Modifier.height(75.dp).fillMaxWidth().clickable { onItemClicked(deviceWithConfig) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton({ onItemClicked(deviceWithConfig) },
                        Modifier.padding(10.dp),
                    ) {
                        Icon(Icons.Filled.Phone, "BLE Device")
                        Text(deviceWithConfig.device.name, Modifier.weight(1f).padding(start = 10.dp))
                    }
                }
            }
        }
    }
}
