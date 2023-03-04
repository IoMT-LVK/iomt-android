package com.iomt.android

import android.Manifest
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.iomt.android.config.ConfigParser

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

import java.util.*

/**
 * [AppCompatActivity] responsible for bluetooth low energy search
 */
class BleSearcher : AppCompatActivity(), SavedDeviceAdapter.OnClickListener {
    private var uiHandler: Handler? = null
    private var menuItem: MenuItem? = null
    private val deviceCells: MutableList<DeviceCell> = ArrayList()
    private var myAdapter: SavedDeviceAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var devTypes: List<DeviceType> = ArrayList()
    private var devs: List<DeviceInfo> = ArrayList()
    private lateinit var httpRequests: Requests
    private val configParser = ConfigParser()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcher)
        val jwt = intent.getStringExtra("JWT")
        val userId = intent.getStringExtra("UserId")
        httpRequests = Requests(this, jwt, userId)
        val actionTypes = Action { args: Array<String?>? ->
            val builder = GsonBuilder()
            val gson = builder.create()
            @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
            val listOfDeviceTypes = object : TypeToken<ArrayList<DeviceType?>?>() {}.type
            devTypes = gson.fromJson(args!![0], listOfDeviceTypes)
        }
        httpRequests.getDeviceTypes(actionTypes)
        val actionDevs = Action { args: Array<String?>? ->
            val builder = GsonBuilder()
            val gson = builder.create()
            @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
            val listOfDevices = object : TypeToken<ArrayList<DeviceInfo?>?>() {}.type
            devs = gson.fromJson(args!![0], listOfDevices)
        }
        httpRequests.getDevices(actionDevs)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION
            )
        }
    }

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.scan) {
            scanLeDevice()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("FOUND DEVICE", result.device.name)
            result.device.name?.let {
                val type = getType(result.device)
                deviceCells.find { it.device.name == result.device.name } ?: run {
                    deviceCells.add(DeviceCell(result.device))
                    Log.d("ADD DEVICE", result.device.name)
                    myAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun scanLeDevice() {
        deviceCells.clear()
        myAdapter?.notifyDataSetChanged()
        uiHandler?.postDelayed({ stopScan() }, SCAN_PERIOD)
        menuItem?.isEnabled = false
        bluetoothLeScanner?.startScan(leScanCallback)
    }

    /**
     * Stop BLE scan
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun stopScan() {
        menuItem?.isEnabled = true
        bluetoothLeScanner?.stopScan(leScanCallback)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Suppress("COMMENTED_OUT_CODE")
    override fun onClickItem(deviceCell: DeviceCell?, device: BluetoothDevice?) {
        stopScan()
        requireNotNull(device)
        val deviceInfo = DeviceInfo(
            device.name,
            device.address,
            getType(device) ?: "UNKNOWN"
        )
        devs.find { it == deviceInfo }?.let {
            httpRequests.sendDevice(deviceInfo)
            // val stringDeviceConfig = httpRequests.getDeviceConfig(1)
            // configParser.parseFromString(stringDeviceConfig ?: "")
            // configParser.parse()
            // Log.w(TAG, configParser.toString())
        }
        finish()
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
        private const val SCAN_PERIOD: Long = 10000
        private const val TAG = "BleScanner"
    }
}
