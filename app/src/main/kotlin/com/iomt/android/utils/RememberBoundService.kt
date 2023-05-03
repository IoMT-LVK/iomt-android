@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.iomt.android.bluetooth.BluetoothLeForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [BleServiceConnection] that represents [ServiceConnection] to [BluetoothLeForegroundService]
 */
class BleServiceConnection : ServiceConnection {
    private val serviceInternal: MutableStateFlow<BluetoothLeForegroundService?> = MutableStateFlow(null)

    /**
     * [BluetoothLeForegroundService]
     */
    val service: StateFlow<BluetoothLeForegroundService?> = serviceInternal

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        if (binder is BluetoothLeForegroundService.BleBinder) {
            serviceInternal.value = binder.getService()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        serviceInternal.value = null
    }
}

/**
 * @return [BluetoothLeForegroundService]
 */
@Composable
fun rememberBoundService(): StateFlow<BluetoothLeForegroundService?> {
    val context = LocalContext.current
    val serviceConnection = remember { BleServiceConnection() }
    var isBound by remember { mutableStateOf(false) }

    DisposableEffect(serviceConnection) {
        if (!isBound) {
            val intent = Intent(context, BluetoothLeForegroundService::class.java)
            isBound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
                isBound = false
            }
        }
    }

    return serviceConnection.service
}
