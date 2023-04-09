/**
 * File containing the scaffold of post-login part of app
 */

package com.iomt.android.jetpack.components

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.iomt.android.entities.AuthInfo
import com.iomt.android.jetpack.navigation.NavRouter
import com.iomt.android.jetpack.navigation.NavRouter.Companion.useMainNavHost
import com.iomt.android.utils.getService
import com.iomt.android.utils.navigate

/**
 * @param navController post-login [NavHostController]
 * @param authInfo [AuthInfo] of current user
 * @param signOut callback invoked on sign out
 * @param onMenuButtonPressed callback invoked on menu button pressed - this should open drawer
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
fun Scaffold(
    navController: NavHostController,
    authInfo: AuthInfo,
    signOut: () -> Unit,
    onMenuButtonPressed: () -> Unit,
) {
    val bluetoothManager: BluetoothManager = LocalContext.getService()

    val knownDevices = remember { bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).toMutableStateList() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = { TopBar(navController, onMenuButtonClicked = onMenuButtonPressed) },
        floatingActionButton = {
            if (navBackStackEntry?.destination?.route == NavRouter.Main.Home.path) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(NavRouter.Main.BleScanner) },
                    shape = ShapeDefaults.Medium,
                ) {
                    Icon(Icons.Default.Add, "Add")
                    Text("Scan")
                }
            } else {
                Unit
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        navController.useMainNavHost(
            authInfo,
            knownDevices,
            Modifier.padding(paddingValues),
            signOut,
            { navController.navigate("${NavRouter.Main.Device}/${knownDevices.indexOf(it)}") },
        ) { knownDevices.add(it) }
    }
}
