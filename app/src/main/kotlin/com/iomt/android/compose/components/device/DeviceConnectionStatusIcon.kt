/**
 * Clickable icon for DeviceHeaterCard of DeviceView
 */

package com.iomt.android.compose.components.device

import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.iomt.android.R
import com.iomt.android.bluetooth.ConnectionStatus
import com.iomt.android.compose.theme.colorScheme

/**
 * @param connectionStatus current [ConnectionStatus]
 * @param changeConnectionStatus callback to change current connection status depending on current [connectionStatus]
 */
@Composable
internal fun DeviceConnectionStatusIcon(connectionStatus: ConnectionStatus, changeConnectionStatus: () -> Unit) {
    OutlinedIconButton({
        changeConnectionStatus()
        Log.d("DeviceConnectionStatusIcon", "clicked")
    }) {
        when (connectionStatus) {
            ConnectionStatus.DISCONNECTED -> Icon(painterResource(R.drawable.nosig), "Disconnected", tint = Color.Red)
            ConnectionStatus.CONNECTING -> Icon(painterResource(R.drawable.blt), "Connecting", tint = Color.Gray)
            ConnectionStatus.CONNECTED -> Icon(painterResource(R.drawable.blt), "Connected", tint = Color.Blue)
        }
    }
}

@Preview
@Composable
private fun DeviceConnectionStatusIconPreview() {
    var connectionStatus by remember { mutableStateOf(ConnectionStatus.DISCONNECTED) }
    MaterialTheme(colorScheme) {
        DeviceConnectionStatusIcon(connectionStatus) {
            when (connectionStatus) {
                ConnectionStatus.DISCONNECTED -> connectionStatus = ConnectionStatus.CONNECTING
                ConnectionStatus.CONNECTING -> Unit
                ConnectionStatus.CONNECTED -> connectionStatus = ConnectionStatus.DISCONNECTED
            }
        }
    }
}
