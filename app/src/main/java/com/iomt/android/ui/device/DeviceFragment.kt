package com.iomt.android.ui.device

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.iomt.android.*
import com.iomt.android.config.configs.CharacteristicConfig
import com.iomt.android.config.configs.DeviceConfig
import com.iomt.android.config.parseConfig
import com.iomt.android.entities.Characteristic
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * Fragment that is responsible for device displaying
 */
class DeviceFragment : Fragment() {
    private var gatt: BluetoothGatt? = null
    private var mqttAndroidClient: MqttAndroidClient? = null
    private var db: DatabaseHelper? = null
    private var senderService: SenderService? = null
    private var devStatus: TextView? = null
    private var devName: TextView? = null
    private var devStPict: ImageView? = null
    private lateinit var deviceConfig: DeviceConfig
    private var inspRate: TextView? = null
    private var expRate: TextView? = null

    private val characteristics: MutableMap<String, Characteristic> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        senderService = SenderService(requireContext(), TEN_MINUTES_IN_MILLISECONDS)
        mqttAndroidClient = MqttAndroidClient(requireContext(), "${R.string.base_uri}:${R.string.mqtt_port}", "")
        mqttAndroidClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                Log.d(loggerTag, "Connection was lost!")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.d(loggerTag, "Message Arrived!: $topic: ${String(message.payload)}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.d(loggerTag, "Delivery Complete!")
            }
        })
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.BLUETOOTH] != true) {
                    // permission was granted, proceed with the operation that requires the permission
                    throw IllegalStateException("Requested permission was not granted")
                }
            }.launch(arrayOf(Manifest.permission.BLUETOOTH))
        }
    }

    private fun getIcon(charName: String): ImageView =
        ImageView(context).apply {
            setBackgroundResource(R.drawable.circular_grey_bordersolid)
            when (charName) {
                "heartRate" -> R.drawable.heart
                "inspRate", "inspirationRate" -> R.drawable.insp
                "expRate", "expirationRate" -> R.drawable.exp
                "steps", "stepsCount" -> R.drawable.steps
                "activity", "activityRate" -> R.drawable.act
                "cadence" -> R.drawable.cadence
                "battery", "batteryRate" -> R.drawable.battery
                else -> R.drawable.circular_grey_bordersolid
            }.let {
                setImageResource(it)
            }
            maxWidth = MAX_ICON_SIZES
            maxHeight = MAX_ICON_SIZES
        }

    private fun createCellLayout(charName: String, prettyCharName: String): Pair<LinearLayout, TextView> {
        val charNameBadge = TextView(context).apply { text = prettyCharName }
        val valueBadge = TextView(context)
        val innerLayout = LinearLayout(context).apply {
            addView(charNameBadge)
            addView(valueBadge)
        }
        val icon = getIcon(charName)
        val linearLayout = LinearLayout(context).apply {
            addView(icon)
            addView(innerLayout)
        }
        return linearLayout to valueBadge
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_device, container, false)
        setHasOptionsMenu(true)

        val device: BluetoothDevice? = requireActivity().intent.getParcelableExtra("Device")
        (activity as AppCompatActivity).supportActionBar?.title = device?.name
        requireContext().getSharedPreferences(requireContext().getString(R.string.ACC_DATA), Context.MODE_PRIVATE)
            .edit()
            .apply { putString("DeviceId", device?.address) }
            .apply()

        Log.d(loggerTag, requireActivity().intent.extras.toString())

        val configString: String = requireNotNull(requireActivity().intent.getStringExtra("configString")) {
            "Could not find a proper config for this device"
        }
        deviceConfig = parseConfig(configString)

        when (deviceConfig.general.type?.lowercase()) {
            "vest" -> R.drawable.hexoskin
            "band" -> R.drawable.band_icon
            else -> R.drawable.circular_grey_bordersolid
        }.let {
            view.findViewById<ImageView>(R.id.deviceIcon)?.setImageResource(it)
        }

        val containerLayout: LinearLayout = view.findViewById(R.id.data_container)
        deviceConfig.general.characteristicNames.forEach { name ->
            val (layout, textView) = createCellLayout(name, deviceConfig.characteristics[name]?.name ?: "")
            containerLayout.addView(layout)
            characteristics[name] = Characteristic(textView)
        }

        devStatus = view.findViewById(R.id.text_status)
        devName = view.findViewById(R.id.device_name)
        devStPict = view.findViewById(R.id.device_st)
        devName?.text = device?.name
        devStatus?.text = "Подключение"
        db = DatabaseHelper(requireContext())
        gatt = device?.connectGatt(requireContext(), true, callback)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDestroy() {
        super.onDestroy()
        gatt?.close()
    }

    private val callback: BluetoothGattCallback = object : BluetoothGattCallback() {
        // Invoked when Bluetooth connection changes
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            requireActivity().runOnUiThread {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    devStatus?.text = "Подключено"
                    devStPict?.setImageResource(R.drawable.blt)
                    this@DeviceFragment.gatt?.discoverServices()
                    senderService?.start()
                } else {
                    devStatus?.text = "Отключено"
                    devStPict?.setImageResource(R.drawable.nosig)
                    senderService?.stop()
                    characteristics.forEach { (_, characteristic) ->
                        characteristic.textView.text = ""
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private fun getInitializedCharacteristic(
            gatt: BluetoothGatt,
            characteristicName: String,
            characteristicConfig: CharacteristicConfig,
        ): Characteristic {
            val service = gatt.getService(UUID.fromString(characteristicConfig.serviceUuid))
            val characteristic = service.getCharacteristic(UUID.fromString(characteristicConfig.characteristicUuid))
                .also { characteristic ->
                    gatt.setCharacteristicNotification(characteristic, true)
                    characteristic.getDescriptor(clientCharacteristicConfigUuid)
                        .let {
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(it)
                        }
                }
            return characteristics.getValue(characteristicName).copy(
                bluetoothGattService = service,
                bluetoothGattCharacteristic = characteristic,
            )
        }

        /**
         * Initialize all the characteristics mentioned in config file
         *
         * @param gatt [BluetoothGatt]
         * @param status
         */
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Listen for Heart Rate notification
                deviceConfig.characteristics.map { (charName, config) ->
                    characteristics[charName] = getInitializedCharacteristic(gatt, charName, config)
                }
            }
        }

        private fun changeCharacteristicLabel(
            characteristicName: String,
            gattCharacteristic: BluetoothGattCharacteristic,
        ) {
            val isUint8 = gattCharacteristic.value[0].toInt().and(0x01).let { it == 0 }
            val format: Int = if (isUint8) {
                BluetoothGattCharacteristic.FORMAT_UINT8
            } else {
                BluetoothGattCharacteristic.FORMAT_UINT16
            }
            val value = gattCharacteristic.getIntValue(format, 1)
            characteristics[characteristicName]?.textView?.text = value.toString()
            characteristics[characteristicName]?.isUpdated = true
        }

        private fun changeAccelerometerLabel(accelerometerCharacteristic: BluetoothGattCharacteristic) {
            val data = accelerometerCharacteristic.value
            val flag = data[0]
            val format = BluetoothGattCharacteristic.FORMAT_UINT16
            var dataIndex = 1
            val isStepCountPresent = flag and 0x01 != 0.toByte()
            val isActivityPresent = flag and 0x02 != 0.toByte()
            val isCadencePresent = flag and 0x04 != 0.toByte()
            if (isStepCountPresent) {
                val stepCount = accelerometerCharacteristic.getIntValue(format, dataIndex)
                characteristics["stepsCount"]?.textView?.text = stepCount.toString()
                dataIndex += 2
            }
            if (isActivityPresent) {
                val activity = accelerometerCharacteristic.getIntValue(format, dataIndex) / 256.0f
                characteristics["activityRate"]?.textView?.text = activity.toString()
                dataIndex += 2
            }
            if (isCadencePresent) {
                val cadence = accelerometerCharacteristic.getIntValue(format, dataIndex)
                characteristics["cadence"]?.textView?.text = cadence.toString()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            requireActivity().runOnUiThread {
                deviceConfig.characteristics.filter { (_, config) ->
                    UUID.fromString(config.characteristicUuid) == characteristic.uuid
                }
                    .map { it.key }
                    .firstOrNull()
                    ?.let {
                        if (it == "accelerometer") {
                            changeAccelerometerLabel(characteristic)
                        } else {
                            changeCharacteristicLabel(it, characteristic)
                        }
                    }

                // Heart Rate Received
                val dfDateAndTime: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val milliseconds: DateFormat = SimpleDateFormat("SSS", Locale.US)
                val now = Date()
                val myDate = dfDateAndTime.format(now)
                val millis = milliseconds.format(now)
                val result = JSONObject().apply {
                    put("Clitime", myDate)
                    put("Millisec", Integer.valueOf(millis))
                }
                characteristics.filter { (_, characteristic) ->
                    characteristic.isUpdated
                }.forEach { (charName, characteristic) ->
                    result.put(charName, Integer.valueOf(characteristic.textView.text.toString()))
                }
                Log.d(loggerTag, result.toString())
                Log.d(loggerTag, db!!.insertNote(result).toString())
                Log.d(loggerTag, db!!.getNotesCount().toString())
            }
        }
    }

    /**
     * @param mqttAndroidClient
     * @param data
     */
    fun sendData(mqttAndroidClient: MqttAndroidClient, data: JSONObject) {
        val prefs = requireContext().getSharedPreferences(
            requireContext().getString(R.string.ACC_DATA),
            Context.MODE_PRIVATE
        )
        val jwt = prefs.getString("JWT", "")
        val userId = prefs.getString("UserId", "")
        val deviceId = prefs.getString("DeviceId", "")
        val options = MqttConnectOptions().apply {
            userName = "username"
            password = jwt!!.toCharArray()
        }
        try {
            mqttAndroidClient.connect(options, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(loggerTag, "Connection Success!")
                    try {
                        val dataString = data.toString()
                        val message = MqttMessage(dataString.toByteArray())
                        Log.d(loggerTag, "Publishing message$message")
                        message.qos = 2
                        message.isRetained = false
                        mqttAndroidClient.publish("c/$userId/$deviceId/data", message)
                        // SenderService.this.dbhelper.deleteNote(ids.get(i));
                    } catch (ex: MqttException) {
                        ex.printStackTrace()
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.d(loggerTag, "Connection Failure!")
                    Log.d(loggerTag, exception.toString())
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun requirePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.BLUETOOTH] != true) {
                    // permission was granted, proceed with the operation that requires the permission
                    throw IllegalStateException("Requested permission was not granted")
                }
            }.launch(arrayOf(Manifest.permission.BLUETOOTH))
        }
    }

    companion object {
        private val heartRateMeasurementServiceUuid =
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

        private val heartRateMeasurementCharacteristicUuid =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

        private val respirationServiceUuid =
            UUID.fromString("3b55c581-bc19-48f0-bd8c-b522796f8e24")

        private val respirationRateMeasurementCharacteristicUuid =
            UUID.fromString("9bc730c3-8cc0-4d87-85bc-573d6304403c")

        private val clientCharacteristicConfigUuid =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val accelerometerServiceUuid =
            UUID.fromString("bdc750c7-2649-4fa8-abe8-fbf25038cda3")

        private val accelerometerMeasurementCharacteristicUuid =
            UUID.fromString("75246a26-237a-4863-aca6-09b639344f43")
        private val loggerTag = DeviceFragment::class.java.simpleName

        private const val MAX_ICON_SIZES = 50
        private const val REQUEST_PERMISSION_BLUETOOTH = 421
        private const val TEN_MINUTES = 10

        private const val TEN_MINUTES_IN_MILLISECONDS = TEN_MINUTES * 60 * 1000
    }
}
