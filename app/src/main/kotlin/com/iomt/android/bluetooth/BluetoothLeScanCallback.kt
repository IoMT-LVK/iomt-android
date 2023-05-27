package com.iomt.android.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
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
    @SuppressLint("MissingPermission")
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
