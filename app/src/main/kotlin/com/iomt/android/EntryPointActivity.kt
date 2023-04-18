package com.iomt.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.iomt.android.bluetooth.BleForegroundService
import com.iomt.android.jetpack.EntryPoint
import com.iomt.android.mqtt.MqttWorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [AppCompatActivity] that is used for [EntryPoint] - compose integration into android
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class EntryPointActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Default)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bleForegroundServiceIntent = Intent(this, BleForegroundService::class.java)
        startForegroundService(bleForegroundServiceIntent)

        val mqttWorkManager = MqttWorkManager.getInstance(this)
        scope.launch { mqttWorkManager.start() }

        if (!checkPermission()) {
            requestPermissions(permissions.toTypedArray(), MASTER_PERMISSION_REQUEST_CODE)
        }

        Log.d(loggerTag, "EntryPointActivity has successfully started")
        setContent {
            EntryPoint()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkPermission() = permissions.all { permission ->
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val permissions = buildList {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_PRIVILEGED)
            add(Manifest.permission.FOREGROUND_SERVICE)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        private const val MASTER_PERMISSION_REQUEST_CODE = 150_601

        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = object { }.javaClass.enclosingClass.simpleName
    }
}
