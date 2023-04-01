/**
 * Component for device list rendering
 */

package com.iomt.android.jetpack.components

import android.Manifest
import android.bluetooth.BluetoothDevice
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * @param title
 * @param devices
 * @param onItemClicked
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceList(title: String, devices: SnapshotStateList<BluetoothDevice>, onItemClicked: (BluetoothDevice) -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth().defaultMinSize(minHeight = 25.dp)) {
        if (devices.isEmpty()) {
            Text("No ${title.lowercase()}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        } else {
            Row { Text(title) }
            devices.map { device ->
                Row(
                    Modifier.height(75.dp).fillMaxWidth().clickable { onItemClicked(device) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton({ onItemClicked(device) },
                        Modifier.padding(10.dp)
                    ) {
                        Icon(Icons.Filled.Phone, "BLE Device")
                        Text(device.name, Modifier.weight(1f).padding(start = 10.dp))
                    }
                }
            }
        }
    }
}
