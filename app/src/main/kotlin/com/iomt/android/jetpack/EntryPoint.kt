/**
 * Entry point of the whole app
 */

package com.iomt.android.jetpack

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.iomt.android.bluetooth.BleForegroundService
import com.iomt.android.http.RequestParams
import com.iomt.android.http.getUserData
import com.iomt.android.jetpack.components.NavViewSystemWithDrawer
import com.iomt.android.jetpack.navigation.NavRouter.Companion.useLoginNavHost
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.mqtt.MqttWorkManager
import kotlinx.coroutines.launch

/**
 * Entry point of application
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Composable
fun EntryPoint() {
    MaterialTheme(colorScheme = colorScheme) {
        val context = LocalView.current.context
        val activity = context as? Activity
        activity?.window?.statusBarColor = colorScheme.primaryContainer.toArgb()
        val preNavController = rememberNavController()
        val scope = rememberCoroutineScope()

        val onLoginSuccess: () -> Unit = {
            val bleForegroundServiceIntent = Intent(context, BleForegroundService::class.java)
            context.startForegroundService(bleForegroundServiceIntent)

            val mqttWorkManager = MqttWorkManager.getInstance(context)

            scope.launch {
                val userData = getUserData()
                mqttWorkManager.start(userData.id)
            }
        }

        val stopServices = {
            val mqttWorkManager = MqttWorkManager.getInstance(context)
            scope.launch { mqttWorkManager.stop() }
        }

        preNavController.useLoginNavHost(onLoginSuccess = onLoginSuccess) {
            NavViewSystemWithDrawer {
                RequestParams.logout()
                preNavController.navigateUp()
                stopServices()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Preview
@Composable
private fun EntryPointPreview() {
    MaterialTheme(colorScheme) { EntryPoint() }
}
