package com.iomt.android.ui.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.iomt.android.CharAdapter;
import com.iomt.android.CharCell;
import com.iomt.android.DatabaseHelper;
import com.iomt.android.R;
import com.iomt.android.SenderService;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class DeviceFragment extends Fragment {
    private BluetoothGatt gatt;

    // Heart Rate Service UUID
    private static UUID HEART_RATE_MEASUREMENT_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");

    // Heart Rate Measurement UUID
    private static UUID HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");

    // Respiration Rate Service UUID
    private static UUID RESPIRATION_SERVICE_UUID = UUID.fromString("3b55c581-bc19-48f0-bd8c-b522796f8e24");

    // Respiration Rate Measurement UUID
    private static UUID RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("9bc730c3-8cc0-4d87-85bc-573d6304403c");

    // UUID for notification
    private static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Accelerometer Service UUID
    private static UUID ACCELEROMETER_SERVICE_UUID = UUID.fromString("bdc750c7-2649-4fa8-abe8-fbf25038cda3");

    // Accelerometer Measurement UUID
    private static UUID ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("75246a26-237a-4863-aca6-09b639344f43");

    private CharAdapter adapter;
    private MqttAndroidClient mqttAndroidClient;
    private DatabaseHelper db;
    private List<CharCell> data = Collections.synchronizedList(new ArrayList<CharCell>());
    private final static String TAG =  DeviceFragment.class.getSimpleName();
    private TextView devStatus, heartRate, respRate, inspRate, expRate, stepsCount, activity, cadence, devName;
    private ImageView devStPict;
    private SenderService senderService;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int period10Minutes = 10;
        int periodInMiliseconds = period10Minutes * 60 * 1000;
        senderService = new SenderService(requireContext(), periodInMiliseconds);


        mqttAndroidClient = new MqttAndroidClient(requireContext(), "tcp://iomt.lvk.cs.msu.su:8883", "");
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Connection was lost!");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d(TAG, "Message Arrived!: " + topic + ": " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Delivery Complete!");
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device, container, false);
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_dev);
        BluetoothDevice device = requireActivity().getIntent().getParcelableExtra("Device");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(device.getName());
        SharedPreferences.Editor editor = requireContext().getSharedPreferences(requireContext().getString(R.string.ACC_DATA), MODE_PRIVATE).edit();
        editor.putString("DeviceId", device.getAddress());
        editor.apply();
        devStatus = view.findViewById(R.id.text_status);
        heartRate = view.findViewById(R.id.text_heart);
        respRate = view.findViewById(R.id.text_resp);
        inspRate = view.findViewById(R.id.text_insp);
        expRate = view.findViewById(R.id.text_exp);
        stepsCount = view.findViewById(R.id.text_steps);
        activity = view.findViewById(R.id.text_act);
        cadence = view.findViewById(R.id.text_cad);
        devName = view.findViewById(R.id.device_name);
        devStPict = view.findViewById(R.id.device_st);

        devName.setText(device.getName());
        devStatus.setText("Подкоючение");

        db = new DatabaseHelper(requireContext());
        gatt = device.connectGatt(requireContext(), true, mCallback);
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onDestroy() {
        super.onDestroy();
        gatt.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private final BluetoothGattCallback mCallback = new BluetoothGattCallback() {

        // Invoked when Bluetooth connection changes
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        devStatus.setText("Подключено");
                        devStPict.setImageResource(R.drawable.blt);
                        DeviceFragment.this.gatt.discoverServices();
                        DeviceFragment.this.senderService.start();
                    } else {

                        devStatus.setText("Отключено");
                        devStPict.setImageResource(R.drawable.nosig);
                        DeviceFragment.this.senderService.stop();
                        heartRate.setText("");
                        respRate.setText("");
                        inspRate.setText("");
                        expRate.setText("");
                        stepsCount.setText("");
                        activity.setText("");
                        cadence.setText("");
                    }
                }
            });

        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Listen for Heart Rate notification
                BluetoothGattService hrSvc = gatt.getService(HEART_RATE_MEASUREMENT_SERVICE_UUID);
                BluetoothGattCharacteristic hrChar = hrSvc.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID);
                gatt.setCharacteristicNotification(hrChar, true);
                BluetoothGattDescriptor descriptor = hrChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (descriptor.getCharacteristic().getUuid().equals(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                // Listen for Respiration Rate notification
                BluetoothGattService respSvc = gatt.getService(RESPIRATION_SERVICE_UUID);
                BluetoothGattCharacteristic respChar = respSvc.getCharacteristic(RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID);
                gatt.setCharacteristicNotification(respChar, true);
                BluetoothGattDescriptor respDescriptor = respChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                respDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(respDescriptor);
            } else if (descriptor.getCharacteristic().getUuid().equals(RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                //Listen for Accelerometer notification
                BluetoothGattService accSvc = gatt.getService(ACCELEROMETER_SERVICE_UUID);
                BluetoothGattCharacteristic accChar = accSvc.getCharacteristic(ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID);
                gatt.setCharacteristicNotification(accChar, true);
                BluetoothGattDescriptor accDescriptor = accChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                accDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(accDescriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] data = characteristic.getValue();
                    UUID uuid = characteristic.getUuid();

                    //Heart Rate Received
                    if (uuid.equals(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                        int flag = data[0];
                        int format;
                        if ((flag & 0x01) == 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        }
                        int heartRate = characteristic.getIntValue(format, 1);
                        DeviceFragment.this.heartRate.setText(String.valueOf(heartRate));
                    }
                    // Respiration Rate Received
                    else if (uuid.equals(RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {

                        byte flag = data[0];
                        int format;
                        if ((flag & 0x01) == 0) {
                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        } else {
                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        }

                        int respRate = characteristic.getIntValue(format, 1);
                        DeviceFragment.this.respRate.setText(String.valueOf(respRate));

                        boolean isInspExpPresent = (flag & 0x02) != 0;
                        DeviceFragment.this.inspRate.setText("");
                        DeviceFragment.this.expRate.setText("");
                        if (isInspExpPresent) {
                            int startOffset = 1 + (format == BluetoothGattCharacteristic.FORMAT_UINT8 ? 1 : 2);
                            boolean inspFirst = (flag & 0x04) == 0;
                            StringBuilder sb_insp = new StringBuilder();
                            StringBuilder sb_exp = new StringBuilder();

                            for (int i = startOffset; i < data.length; i += 2) {
                                float value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i) / 32.0f;
                                if (inspFirst) {
                                    sb_insp.append(value);
                                    inspFirst = false;
                                } else {
                                    sb_exp.append(value);
                                    inspFirst = true;
                                }
                            }
                            DeviceFragment.this.inspRate.setText(sb_insp.toString());
                            DeviceFragment.this.expRate.setText(sb_exp.toString());
                        }
                    }

                    // Accelerometer data received
                    else if (uuid.equals((ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID))) {
                        byte flag = data[0];
                        int format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        int dataIndex = 1;

                        boolean isStepCountPresent = (flag & 0x01) != 0;
                        boolean isActivityPresent = (flag & 0x02) != 0;
                        boolean isCadencePresent = (flag & 0x04) != 0;

                        if (isStepCountPresent) {
                            int stepCount = characteristic.getIntValue(format, dataIndex);
                            DeviceFragment.this.stepsCount.setText(String.valueOf(stepCount));
                            dataIndex = dataIndex + 2;
                        }

                        if (isActivityPresent) {
                            float activity = characteristic.getIntValue(format, dataIndex) / 256.0f;
                            DeviceFragment.this.activity.setText(String.valueOf(activity));
                            dataIndex = dataIndex + 2;
                        }

                        if (isCadencePresent) {
                            int cadence = characteristic.getIntValue(format, dataIndex);
                            DeviceFragment.this.cadence.setText(String.valueOf(cadence));
                        }
                    }
                    DateFormat dfDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    DateFormat millisec = new SimpleDateFormat("SSS");
                    Date now = new Date();
                    String mydate = dfDateAndTime.format(now);
                    String millis = millisec.format(now);


                    JSONObject result = new JSONObject();
                    try {
                        String val = DeviceFragment.this.heartRate.getText().toString();
                        result.put("HeartRate", val.length() != 0 ? Integer.valueOf(val) : JSONObject.NULL);
                        val = DeviceFragment.this.stepsCount.getText().toString();
                        result.put("Steps", val.length() != 0 ? Integer.valueOf(val) : JSONObject.NULL);
                        val = DeviceFragment.this.activity.getText().toString();
                        result.put("Activity", val.length() != 0 ? Float.valueOf(val) : JSONObject.NULL);
                        val = DeviceFragment.this.cadence.getText().toString();
                        result.put("Cadence", val.length() != 0 ? Integer.valueOf(val) : JSONObject.NULL);
                        result.put("Clitime", mydate);
                        result.put("Millisec", Integer.valueOf(millis));
                        val = DeviceFragment.this.respRate.getText().toString();
                        result.put("RespRate", val.length() != 0 ? Integer.valueOf(val) : JSONObject.NULL);
                        val = DeviceFragment.this.inspRate.getText().toString();
                        result.put("Insp", val.length() != 0 ? Float.valueOf(val) : JSONObject.NULL);
                        val = DeviceFragment.this.expRate.getText().toString();
                        result.put("Exp", val.length() != 0 ? Float.valueOf(val) : JSONObject.NULL);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Log.d(TAG, result.toString());
                    Log.d(TAG, String.valueOf(db.insertNote(result)));
                    Log.d(TAG, String.valueOf(db.getNotesCount()));
                }
            });
        }
    };

    public void send_data(final MqttAndroidClient mqttAndroidClient, JSONObject data) {
        SharedPreferences prefs = requireContext().getSharedPreferences(requireContext().getString(R.string.ACC_DATA), MODE_PRIVATE);
        String JWT = prefs.getString("JWT", "");
        String UserId = prefs.getString("UserId", "");
        String DeviceId = prefs.getString("DeviceId", "");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("username");
        options.setPassword(JWT.toCharArray());
        try {
            mqttAndroidClient.connect(options, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connection Success!");

                    try {
                        String data_str = data.toString();
                        MqttMessage message = new MqttMessage(data_str.getBytes());
                        Log.d(TAG, "Publishing message" + message);
                        message.setQos(2);
                        message.setRetained(false);
                        mqttAndroidClient.publish("c/" + UserId + "/" + DeviceId + "/data", message);
                        //SenderService.this.dbhelper.deleteNote(ids.get(i));
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Connection Failure!");
                    Log.d(TAG, String.valueOf(exception));
                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

}