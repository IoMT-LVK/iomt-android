/**
 * Header of DeviceView
 */

package com.iomt.android.compose.components.device

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.bluetooth.ConnectionStatus

/**
 * @param bluetoothDevice currently selected [BluetoothDevice]
 * @param connectionStatus current [ConnectionStatus]
 * @param changeConnectionStatus callback to change current [connectionStatus] depending on its value
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceHeaderCard(
    bluetoothDevice: BluetoothDevice,
    connectionStatus: ConnectionStatus,
    changeConnectionStatus: () -> Unit,
) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(100.dp).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Icon(painterResource(R.drawable.default_device), "Icon", Modifier.size(50.dp))
            Column(Modifier.fillMaxWidth().weight(1f)) {
                Text(bluetoothDevice.name, fontSize = TextUnit(6f, TextUnitType.Em))
                when (connectionStatus) {
                    ConnectionStatus.DISCONNECTED -> Text("Disconnected", fontWeight = FontWeight.Light)
                    ConnectionStatus.CONNECTING -> Text("Connecting", fontWeight = FontWeight.Light)
                    ConnectionStatus.CONNECTED -> Text("Connected", fontWeight = FontWeight.Light)
                }
            }
            DeviceConnectionStatusIcon(connectionStatus, changeConnectionStatus)
        }
    }
}
