@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.jetpack.components

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.iomt.android.R
import com.iomt.android.entities.AuthInfo
import com.iomt.android.jetpack.view.*

/**
 * @property iconId
 * @property path
 */
enum class NavRouter(val iconId: Int, val path: String) {
    ACCOUNT(R.drawable.ic_menu_account, "Account"),
    BLE_SCANNER(R.drawable.blt, "Ble Scanner"),
    DEVICE(R.drawable.default_device, "Device {id}"),
    HOME(R.drawable.ic_menu_home, "Home"),
    SETTINGS(R.drawable.ic_menu_settings, "Settings"),
    ;
    companion object {
        val default = HOME
    }
}

/**
 * @param navController
 * @param bluetoothManager
 * @param authInfo
 * @param signOut
 * @param onMenuButtonPressed
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
fun Scaffold(
    navController: NavHostController,
    bluetoothManager: BluetoothManager,
    authInfo: AuthInfo,
    signOut: () -> Unit,
    onMenuButtonPressed: () -> Unit,
) {
    var knownDevices = remember { mutableStateListOf<BluetoothDevice>() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = { TopBar(navController, onMenuButtonClicked = onMenuButtonPressed) },
        floatingActionButton = {
            if (navBackStackEntry?.destination?.route == NavRouter.HOME.path) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(NavRouter.BLE_SCANNER.path) },
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
        NavHost(navController, modifier = Modifier.padding(paddingValues), startDestination = NavRouter.default.path) {
            composable(NavRouter.HOME.path) { HomeView(knownDevices) { knownDevices.add(it) } }
            composable(NavRouter.SETTINGS.path) { SettingsView(signOut) }
            // composable(NavRouter.DEVICE.path) { DeviceView() }
            composable(NavRouter.ACCOUNT.path) { AccountView(knownDevices) }
            composable(NavRouter.BLE_SCANNER.path) { BleScannerView(navController, bluetoothManager) { knownDevices.add(it) } }
        }
    }
}
