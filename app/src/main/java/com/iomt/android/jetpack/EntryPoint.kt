@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package com.iomt.android.jetpack

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iomt.android.entities.AuthInfo
import com.iomt.android.jetpack.components.NavViewSystemWithDrawer
import com.iomt.android.jetpack.view.EmailConfView

/**
 * @param bluetoothManager
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Composable
fun EntryPoint(bluetoothManager: BluetoothManager) {
    Surface {
        val activity = LocalView.current.context as Activity
        activity.window.statusBarColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
        val preNavController = rememberNavController()

        var authInfo by remember { mutableStateOf(AuthInfo.empty) }

        NavHost(preNavController, startDestination = "login") {
            composable("login") { LoginView(preNavController) { authInfo = it } }
            composable("emailConf") { EmailConfView(preNavController) }
            composable("registration") { /* RegistrationView() */ }
            composable("navViewSystem") {
                NavViewSystemWithDrawer(bluetoothManager, authInfo) {
                    authInfo = AuthInfo.empty
                    preNavController.navigate("login")
                }
            }
        }
    }
}
