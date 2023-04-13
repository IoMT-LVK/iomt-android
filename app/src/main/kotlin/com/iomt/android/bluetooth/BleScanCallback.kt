package com.iomt.android.bluetooth

import android.Manifest
import android.annotation.SuppressLint
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
class BleScanCallback(
    private val connectedDevices: SnapshotStateList<BluetoothDevice>,
    private val foundDevices: SnapshotStateList<BluetoothDevice>,
) : ScanCallback() {
    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onScanResult(callbackType: Int, result: ScanResult) {
        val device = result.device
        Log.d(loggerTag, "${device.name} - ${device.address}")
        if (device !in connectedDevices && device !in foundDevices) {
            device.name?.let { foundDevices.add(device) }
        }
    }
    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = object { }.javaClass.enclosingClass.simpleName
    }
}
