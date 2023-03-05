package com.iomt.android

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iomt.android.entities.DeviceInfo
import com.iomt.android.entities.DeviceType
import com.iomt.android.ui.home.scanLeDeviceWithPermissionCheck
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

import java.util.*

/**
 * [AppCompatActivity] responsible for bluetooth low energy search
 */
@RuntimePermissions
class BleSearcher : AppCompatActivity(), SavedDeviceAdapter.OnClickListener {
    private var uiHandler: Handler? = null
    private var menuItem: MenuItem? = null
    private val deviceCells: MutableList<DeviceCell> = ArrayList()
    private var myAdapter: SavedDeviceAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var devTypes: List<DeviceType> = ArrayList()
    private var devs: List<DeviceInfo> = ArrayList()
    private lateinit var httpRequests: Requests

    @RequiresApi(Build.VERSION_CODES.S)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcher)
        val jwt = intent.getStringExtra("JWT")
        val userId = intent.getStringExtra("UserId")
        httpRequests = Requests(jwt, userId)
        httpRequests.getDeviceTypes { devTypes = it }
        httpRequests.getDevices { devs = it }
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        uiHandler = Handler(Looper.getMainLooper())
        val toolbar = findViewById<View>(R.id.toolbar_searcher) as Toolbar
        setSupportActionBar(toolbar)
        val recyclerView = findViewById<View>(R.id.searcher_recycler_view) as RecyclerView
        recyclerView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        myAdapter = SavedDeviceAdapter(LayoutInflater.from(this), deviceCells, this)
        recyclerView.adapter = myAdapter
        val permissionsToRequest: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        requestPermissions(permissionsToRequest, PERMISSION_REQUEST_FINE_LOCATION)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun getType(device: BluetoothDevice?): String? = devTypes.find { device?.name?.startsWith(it.prefix) ?: false }?.deviceType

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                menuItem?.isEnabled = true
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Location not granted")
                builder.setMessage("Since location access has not been granted, this app will not be able to discover nearby bluetooth devices.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.show()
                menuItem?.isEnabled = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_searcher, menu)
        menuItem = menu.findItem(R.id.scan)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.scan) {
            scanLeDeviceWithPermissionCheck()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(TAG, "Found device: ${result.device.name}")
            result.device.name?.let {
                deviceCells.find { it.device.name == result.device.name } ?: run {
                    deviceCells.add(DeviceCell(result.device))
                    Log.d(TAG, "Added device: ${result.device.name}")
                    myAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * Start BLE scan
     *
     * kapt generates [scanLeDeviceWithPermissionCheck] for [scanLeDevice] processing permission requests
     */
    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    @NeedsPermission(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    fun scanLeDevice() {
        deviceCells.clear()
        myAdapter?.notifyDataSetChanged()
        uiHandler?.postDelayed({ stopScan() }, SCAN_PERIOD)
        menuItem?.isEnabled = false
        bluetoothLeScanner?.startScan(leScanCallback)
    }

    /**
     * Stop BLE scan
     *
     * kapt generates [stopScanWithPermissionCheck] for [stopScan] processing permission requests
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @NeedsPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        menuItem?.isEnabled = true
        bluetoothLeScanner?.stopScan(leScanCallback)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    override fun onClickItem(deviceCell: DeviceCell?, device: BluetoothDevice?) {
        stopScan()
        requireNotNull(device)
        val deviceInfo = DeviceInfo(
            device.name,
            device.address,
            getType(device) ?: "UNKNOWN"
        )
        devs.find { it == deviceInfo }?.let { httpRequests.sendDevice(deviceInfo) }
        finish()
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
        private const val SCAN_PERIOD: Long = 10000
        private const val TAG = "BleScanner"
    }
}
