package com.iomt.android.ui.home

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.iomt.android.*
import com.iomt.android.config.ConfigParser
import com.iomt.android.config.configs.DeviceConfig
import java.util.*

/**
 * Displays list of Hexoskin Devices
 */
class HomeFragment : Fragment(), DeviceAdapter.OnClickListener {
    private var uiHandler: Handler? = null
    private var menuItem: MenuItem? = null
    private val deviceCells: MutableList<DeviceCell> = ArrayList()
    private var myAdapter: DeviceAdapter? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var jwt: String? = null
    private var userId: String? = null
    private var httpRequests: HTTPRequests? = null
    private var deviceInfos: List<DeviceInfo> = ArrayList()
    private val builder = GsonBuilder()
    private val gson = builder.create()
    private var config: DeviceConfig? = null
    private val configParser = ConfigParser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        }
        if (bluetoothAdapter == null) {
            Toast.makeText(
                requireContext(),
                R.string.error_bluetooth_not_supported,
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().finish()
            return
        }

        if (checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_READ_CONTACTS
            )
        }

        statusCheck()
        mBluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        uiHandler = Handler(Looper.getMainLooper())
        val prefs = requireActivity().getSharedPreferences(
            requireContext().getString(R.string.ACC_DATA),
            Context.MODE_PRIVATE
        )
        jwt = prefs.getString("JWT", "")
        userId = prefs.getString("UserId", "")
        httpRequests = HTTPRequests(requireContext(), jwt, userId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION
            )
        }
    }

    private fun statusCheck() {
        val manager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your GPS seems to be disabled, but it is needed for Bluetooth correct work")
            .setCancelable(false)
            .setPositiveButton("Enable") { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                )
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.cancel()
                requireActivity().finish()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            requireActivity().finish()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
//        val toolbar = view.findViewById(R.id.toolbar_list) as Toolbar?
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Устройства"
        val recyclerView: RecyclerView = view.findViewById(R.id.my_recycler_view)
        recyclerView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        myAdapter = DeviceAdapter(inflater, deviceCells, this)
        recyclerView.adapter = myAdapter
        val noDev = view.findViewById<TextView>(R.id.text_no_dev)
        noDev.visibility = View.GONE
        val actionCells = Action { args: Array<String?>? ->
            uiHandler?.post {
                val listOfDevices = object: TypeToken<ArrayList<DeviceInfo?>?>() {}.type
                deviceInfos = gson.fromJson(args!![0], listOfDevices)
                noDev.visibility = View.GONE
                if (deviceInfos.isEmpty()) {
                    noDev.visibility = View.VISIBLE
                }
            }
        }
        val configString = httpRequests!!.getDeviceConfig(1)
        config = configParser.parseFromString(configString)
//        checkPermissions()
        httpRequests!!.getDevices(actionCells)
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Location not granted")
                builder.setMessage("Since location access has not been granted, this app will not be able to discover nearby bluetooth devices.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_device_list, menu)
        menuItem = menu.findItem(R.id.scan)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.scan) {
            scanLeDevice()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private val mLeScanCallback: ScanCallback = object : ScanCallback() {
        private val TAG = ScanCallback::class.java.toString()
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (result.device.name != null) {
                val device = result.device
                val isInSavedDeviceList = deviceInfos.any { it.name == device.name && it.address == device.address }
                val isAlreadyPresent = deviceCells.any { it.device.address == device.address }
                // TODO: should filter devices in a smart way
                Log.w("CONFIG", result.device.name)
                val isConfigured = result.device.name.matches((config?.general?.nameRegex ?: "").toRegex())
                if (!isInSavedDeviceList && !isAlreadyPresent && isConfigured) {
                    Log.d(TAG, "New device found: ${device.name} (${device.type})")
                    deviceCells.add(DeviceCell(device))
                    myAdapter!!.notifyDataSetChanged()
                }
            }
        }
    }

//    private fun checkPermissions() {
//        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
//            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        } else {
//            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE)
//        }
//        val missingPermissions = requiredPermissions.filter { permission ->
//            checkSelfPermission(context!!, permission) != PackageManager.PERMISSION_GRANTED
//        }
//        if (missingPermissions.isEmpty()) {
//            scanLeDevice()
//        } else {
//            requestPermissions(missingPermissions.toTypedArray(), BLUETOOTH_PERMISSION_REQUEST_CODE)
//        }
//    }


    // Scan for BLE devices
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun scanLeDevice() {
        // Clear devices
        deviceCells.clear()

        // reload table
        myAdapter!!.notifyDataSetChanged()
        //stopScan();

        // Stops scanning after a pre-defined scan period.
        uiHandler!!.postDelayed({ stopScan() }, SCAN_PERIOD)
        menuItem!!.isEnabled = false

        // scan
        //bluetoothAdapter.startLeScan(mLeScanCallback);
        mBluetoothLeScanner!!.startScan(mLeScanCallback)
        Log.d(TAG, "Scan started")
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun stopScan() {
        menuItem!!.isEnabled = true
        //bluetoothAdapter.stopLeScan(mLeScanCallback);
        mBluetoothLeScanner!!.stopScan(mLeScanCallback)
        Log.d(TAG, "Scan stopped")
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onClickItem(deviceCell: DeviceCell?, device: BluetoothDevice?) {
        stopScan()
        // Show Device Detail
        val intent = Intent(activity, DeviceActivity::class.java)
        intent.putExtra("Device", device)
        intent.putExtra("deviceConfig", configParser.toString())
        startActivity(intent)
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
        private const val REQUEST_ENABLE_BT = 1
        private const val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 9999

        // Stops scanning after 60 seconds.
        private const val SCAN_PERIOD: Long = 60000
        private val TAG = this::class.java.toString()
    }
}