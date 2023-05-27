/**
 * File that contains compose-styled service binder
 */

@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.iomt.android.bluetooth.BluetoothLeForegroundService
import com.iomt.android.bluetooth.BluetoothLeLegacyService
import com.iomt.android.bluetooth.BluetoothLeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * [BleServiceConnection] that represents [ServiceConnection] to [BluetoothLeForegroundService]
 */
@RequiresApi(Build.VERSION_CODES.S)
class BleServiceConnection : ServiceConnection {
    private val serviceInternal: MutableStateFlow<BluetoothLeForegroundService?> = MutableStateFlow(null)

    /**
     * [BluetoothLeForegroundService]
     */
    val service: StateFlow<BluetoothLeForegroundService?> = serviceInternal

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        if (binder is BluetoothLeForegroundService.BluetoothLeBinder) {
            serviceInternal.value = binder.getService()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        serviceInternal.value = null
    }
}

/**
 * [BleServiceConnection] that represents [ServiceConnection] to [BluetoothLeForegroundService]
 */
class BleLegacyServiceConnection : ServiceConnection {
    private val serviceInternal: MutableStateFlow<BluetoothLeLegacyService?> = MutableStateFlow(null)

    /**
     * [BluetoothLeForegroundService]
     */
    val service: StateFlow<BluetoothLeLegacyService?> = serviceInternal

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        if (binder is BluetoothLeLegacyService.BluetoothLeBinder) {
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
fun rememberBoundService(): StateFlow<BluetoothLeService?> {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val serviceConnection = remember {
            BleServiceConnection()
        }
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
        serviceConnection.service
    } else {
        val serviceConnection = remember {
            BleLegacyServiceConnection()
        }
        var isBound by remember { mutableStateOf(false) }

        DisposableEffect(serviceConnection) {
            if (!isBound) {
                val intent = Intent(context, BluetoothLeLegacyService::class.java)
                isBound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }

            onDispose {
                if (isBound) {
                    context.unbindService(serviceConnection)
                    isBound = false
                }
            }
        }
        serviceConnection.service
    }
}
