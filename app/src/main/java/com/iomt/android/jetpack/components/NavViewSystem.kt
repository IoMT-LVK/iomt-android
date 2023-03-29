/**
 * Entry point on an app after login was successful
 */

package com.iomt.android.jetpack.components

import android.Manifest
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.iomt.android.entities.AuthInfo
import com.iomt.android.jetpack.components.drawer.Drawer

/**
 * @param bluetoothManager
 * @param authInfo
 * @param signOut
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Composable
fun NavViewSystemWithDrawer(bluetoothManager: BluetoothManager, authInfo: AuthInfo, signOut: () -> Unit) {
    val navController = rememberNavController()
    Drawer(navController) { openDrawer ->
        Scaffold(navController, bluetoothManager, authInfo, signOut, openDrawer)
    }
}
