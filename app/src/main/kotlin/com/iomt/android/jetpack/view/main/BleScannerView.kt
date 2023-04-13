/**
 * Bluetooth LE scanner view
 */

package com.iomt.android.jetpack.view.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.iomt.android.EntryPointActivity
import com.iomt.android.bluetooth.BleScanCallback
import com.iomt.android.config.configs.toCharacteristicEntities
import com.iomt.android.config.parseConfig
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.room.characteristic.CharacteristicRepository
import com.iomt.android.room.device.DeviceRepository
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkEntity
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.utils.getService
import com.iomt.android.utils.rememberBoundService
import com.iomt.android.utils.withLoading

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val scanningPeriod = 30.seconds

/**
 * @param navigateBack callback to go to previous view (HomeView)
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
@Composable
fun BleScannerView(navigateBack: () -> Unit) {
    val bluetoothManager: BluetoothManager = LocalContext.getService()
    val bluetoothAdapter = bluetoothManager.adapter
    val enableBluetoothContract = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != Activity.RESULT_OK) {
            navigateBack()
        }
    }
    if (bluetoothAdapter?.isEnabled == false) {
        enableBluetoothContract.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    val deviceRepository = DeviceRepository(LocalContext.current)
    val characteristicRepository = CharacteristicRepository(LocalContext.current)
    val deviceCharacteristicRepository = DeviceCharacteristicLinkRepository(LocalContext.current)
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    val bleForegroundService by rememberBoundService().collectAsState()

    val scope = rememberCoroutineScope()

    withLoading(bleForegroundService) { bleService ->
        val foundDevices = remember { bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).toMutableStateList() }
        val connectedDevices = remember { bleService.getConnectedDevices().toMutableStateList() }
        val leScanCallback = BleScanCallback(connectedDevices, foundDevices)

        var isScanning by remember { mutableStateOf(false) }
        bluetoothLeScanner.startScan(leScanCallback).also { isScanning = true }
        LaunchedEffect(isScanning) {
            if (isScanning) {
                delay(scanningPeriod)
                bluetoothLeScanner.stopScan(leScanCallback)
                isScanning = false
            }
        }

        Column(Modifier.fillMaxSize()) {
            foundDevices.map { device ->
                Row(
                    Modifier.fillMaxWidth()
                        .clickable {
                            scope.launch {
                                val deviceId = deviceRepository.getByMacOrSave(device.name, device.address).id!!
                                val stubConfig = parseConfig(
                                    EntryPointActivity::class.java.classLoader?.getResource("ConfigParser/band.toml")?.readText()!!
                                )
                                val charEntities = stubConfig.characteristics.toCharacteristicEntities()
                                characteristicRepository.insertAllIfNotPresent(charEntities)
                                    .map { charId -> DeviceCharacteristicLinkEntity(deviceId, charId) }
                                    .let { deviceCharacteristicRepository.insertAllIfNotPresent(it) }

                                bleService.connectDevice(device, stubConfig).also { MainScope().launch { navigateBack() } }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(device.name)
                    Spacer(Modifier.padding(10.dp))
                    Text(device.address)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
@Preview
@Composable
private fun BleScannerViewPreview() {
    MaterialTheme(colorScheme) { BleScannerView { } }
}
