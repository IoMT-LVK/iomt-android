/**
 * Entry point of the whole app
 */

package com.iomt.android.compose

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

import com.iomt.android.bluetooth.BluetoothLeForegroundService
import com.iomt.android.compose.components.NavViewSystemWithDrawer
import com.iomt.android.compose.navigation.NavRouter.Companion.useLoginNavHost
import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.dbcleaner.CleanerWorkManager
import com.iomt.android.http.RequestParams
import com.iomt.android.http.getUserData
import com.iomt.android.mqtt.MqttWorkManager
import com.iomt.android.room.AppDatabase
import com.iomt.android.statsitics.StatisticsWorkManager

import kotlinx.coroutines.Dispatchers
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
            val bluetoothLeForegroundServiceIntent = Intent(context, BluetoothLeForegroundService::class.java)
            context.startForegroundService(bluetoothLeForegroundServiceIntent)

            val mqttWorkManager = MqttWorkManager.getInstance(context)
            val cleanerWorkManager = CleanerWorkManager.getInstance(context)
            val statisticsWorkManager = StatisticsWorkManager.getInstance(context)

            scope.launch(Dispatchers.Default) {
                val userData = getUserData()
                mqttWorkManager.start(userData.id)
                cleanerWorkManager.start()
                statisticsWorkManager.start()
            }
        }

        val onLogOut = {
            val cleanerWorkManager = CleanerWorkManager.getInstance(context)
            val mqttWorkManager = MqttWorkManager.getInstance(context)
            val statisticsWorkManager = StatisticsWorkManager.getInstance(context)
            scope.launch(Dispatchers.Default) {
                mqttWorkManager.stop()
                cleanerWorkManager.stop()
                statisticsWorkManager.stop()
            }
            scope.launch(Dispatchers.IO) { AppDatabase.getInstance(context).clearAllTables() }
        }

        preNavController.useLoginNavHost(onLoginSuccess = onLoginSuccess) {
            NavViewSystemWithDrawer {
                RequestParams.logout()
                preNavController.navigateUp()
                onLogOut()
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
