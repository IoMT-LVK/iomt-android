package com.iomt.android.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Default Bluetooth [ScanCallback]
 *
 * @property connectedDevices [SnapshotStateList] of connected devices
 * @property foundDevices [SnapshotStateList] of found devices
 */
class BluetoothLeScanCallback(
    private val connectedDevices: SnapshotStateList<BluetoothDevice>,
    private val foundDevices: SnapshotStateList<BluetoothDevice>,
) : ScanCallback() {
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION])
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        val device = result.device
        if (device !in connectedDevices && device !in foundDevices && device.name != null) {
            Log.d(loggerTag, "${device.name} - ${device.address}")
            device.name?.let { foundDevices.add(device) }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e(loggerTag, "Scan failed with [$errorCode] error code.")
    }
    companion object {
        private val loggerTag = BluetoothLeScanCallback::class.java.simpleName
    }
}
