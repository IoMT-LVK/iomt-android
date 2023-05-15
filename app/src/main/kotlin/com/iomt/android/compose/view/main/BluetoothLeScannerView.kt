/**
 * Bluetooth LE scanner view
 */

package com.iomt.android.compose.view.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.iomt.android.bluetooth.BluetoothLeScanCallback
import com.iomt.android.compose.components.ConfigItem
import com.iomt.android.compose.components.DeviceItem
import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.configs.toCharacteristicEntities
import com.iomt.android.http.getDeviceTypes
import com.iomt.android.room.characteristic.CharacteristicRepository
import com.iomt.android.room.device.DeviceRepository
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkEntity
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkRepository
import com.iomt.android.utils.*

import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.*

private val scanningPeriod = 30.seconds

/**
 * @param mutableFloatingButtonBuilder MutableState of FAB builder - used for setting the FAB
 * @param navigateBack callback to go to previous view (HomeView)
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
@Composable
fun BluetoothLeScannerView(
    mutableFloatingButtonBuilder: MutableFloatingButtonBuilder,
    navigateBack: () -> Unit,
) {
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

    val bluetoothLeForegroundService by rememberBoundService().collectAsState()

    val scope = rememberCoroutineScope()

    withLoading(bluetoothLeForegroundService) { bleService ->
        val foundDevices = remember { bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).toMutableStateList() }
        val connectedDevices = remember { bleService.getConnectedDevices().toMutableStateList() }
        val leScanCallback by remember { mutableStateOf(BluetoothLeScanCallback(connectedDevices, foundDevices)) }

        var isScanning by remember { mutableStateOf(false) }
        var isScanRequested by remember { mutableStateOf(false) }

        mutableFloatingButtonBuilder.value = {
            val floatingActionButtonContent: @Composable () -> Unit = {
                when (isScanning) {
                    true -> {
                        Icon(Icons.Default.Clear, "Stop")
                        Text("Stop scan")
                    }

                    false -> {
                        Icon(Icons.Default.Add, "Start")
                        Text("Start scan")
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { isScanRequested = !isScanRequested },
                shape = ShapeDefaults.Medium,
            ) { floatingActionButtonContent() }
        }
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        val stopScan: suspend (CoroutineContext) -> Unit = { coroutineContext ->
            withContext(coroutineContext) {
                bluetoothLeScanner.stopScan(leScanCallback).also {
                    Log.d("BleScanner", "Scan stopped after $scanningPeriod")
                }
            }
        }

        @Suppress("TOO_MANY_LINES_IN_LAMBDA")
        val startScanWithTimeout: suspend (CoroutineContext) -> Unit = { coroutineContext ->
            withContext(coroutineContext) {
                bluetoothLeScanner.startScan(emptyList(), scanSettings, leScanCallback)
                    .also { Log.d("BleScanner", "Scan started") }
                isScanning = true
                delay(scanningPeriod)
                bluetoothLeScanner.flushPendingScanResults(leScanCallback)
                bluetoothLeScanner.stopScan(leScanCallback).also {
                    Log.d("BleScanner", "Scan stopped after $scanningPeriod")
                }
            }
        }

        LaunchedEffect(isScanRequested) {
            if (isScanRequested && !isScanning) {
                isScanRequested = false
                startScanWithTimeout(Dispatchers.Main)
                isScanning = false
            } else if (isScanRequested) {
                stopScan(Dispatchers.Main)
                isScanning = false
            }
        }

        var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
        var deviceNameSubstring by remember { mutableStateOf("") }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            selectedDevice?.let { device ->
                val configs = remember { mutableStateListOf<DeviceConfig>() }
                val updateConfigsWithDebounce: (String) -> Unit = withDebounce(200.milliseconds, scope) {
                    with(configs) {
                        val devices = getDeviceTypes(it)
                        clear()
                        addAll(devices)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.Center) {
                    OutlinedTextField(
                        value = deviceNameSubstring,
                        onValueChange = { value -> deviceNameSubstring = value },
                        label = { Text("Search") },
                    )
                }

                Divider()

                LaunchedEffect(deviceNameSubstring) {
                    updateConfigsWithDebounce(deviceNameSubstring)
                }

                configs.forEach { config ->
                    ConfigItem(config) {
                        scope.launch(Dispatchers.Default) {
                            val deviceId = deviceRepository.getByMacOrSave(device.name, device.address).id!!

                            config.characteristics.toCharacteristicEntities()
                                .let { characteristicRepository.insertAllIfNotPresent(it) }
                                .map { charId -> DeviceCharacteristicLinkEntity(deviceId, charId) }
                                .let { deviceCharacteristicRepository.insertAllIfNotPresent(it) }

                            bleService.connectDevice(device, config).also { MainScope().launch { navigateBack() } }
                        }
                    }
                }
            } ?: run {
                foundDevices.forEach { device ->
                    DeviceItem(device.name, device.address) {
                        bluetoothLeScanner.stopScan(leScanCallback)
                        selectedDevice = device
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
@Preview
@Composable
private fun BluetoothLeScannerViewPreview() {
    val mutableFloatingButtonBuilder = remember { mutableStateOf<FloatingButtonBuilder>({}) }
    MaterialTheme(colorScheme) { BluetoothLeScannerView(mutableFloatingButtonBuilder) { } }
}
