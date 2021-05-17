//package com.iomt.android;
//
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothProfile;
//import android.os.Build;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.customview.widget.ViewDragHelper;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//import androidx.navigation.ui.AppBarConfiguration;
//import androidx.navigation.ui.NavigationUI;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.alibaba.fastjson.JSON;
//import com.google.android.material.navigation.NavigationView;
//
//import org.eclipse.paho.android.service.MqttAndroidClient;
//import org.eclipse.paho.client.mqttv3.IMqttActionListener;
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//
//import java.lang.reflect.Field;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//public class DeviceActivity extends AppCompatActivity {
//
//    private BluetoothGatt gatt;
//
//    // Heart Rate Service UUID
//    private static UUID HEART_RATE_MEASUREMENT_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
//
//    // Heart Rate Measurement UUID
//    private static UUID HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
//
//    // Respiration Rate Service UUID
//    private static UUID RESPIRATION_SERVICE_UUID = UUID.fromString("3b55c581-bc19-48f0-bd8c-b522796f8e24");
//
//    // Respiration Rate Measurement UUID
//    private static UUID RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("9bc730c3-8cc0-4d87-85bc-573d6304403c");
//
//    // UUID for notification
//    private static UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//
//    // Accelerometer Service UUID
//    private static UUID ACCELEROMETER_SERVICE_UUID = UUID.fromString("bdc750c7-2649-4fa8-abe8-fbf25038cda3");
//
//    // Accelerometer Measurement UUID
//    private static UUID ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("75246a26-237a-4863-aca6-09b639344f43");
//
//    private CharAdapter adapter;
//    private MqttAndroidClient mqttAndroidClient;
//
//    private DatabaseHelper db;
//
//    //private List<String> data = Collections.synchronizedList(new ArrayList<String>());
//    private List<CharCell> data = Collections.synchronizedList(new ArrayList<CharCell>());
//
//    private final static String TAG = DeviceActivity.class.getSimpleName();
//
//    private MenuItem _menuItem;
//
//    private AppBarConfiguration mAppBarConfiguration;
//
//
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_device);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        BluetoothDevice _device = getIntent().getParcelableExtra("Device");
//        getSupportActionBar().setTitle(_device.getName());
//
//        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
//        recyclerView.setHasFixedSize(true);
//
//        RecyclerView.LayoutManager _layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
//        recyclerView.setLayoutManager(_layoutManager);
//
//        // Table view data
//        data.add(new CharCell( "Connecting ..."));
//        data.add(new CharCell("HEART RATE: "));
//        data.add(new CharCell("RESP. RATE: "));
//        data.add(new CharCell("INSP: "));
//        data.add(new CharCell("EXP: "));
//        data.add(new CharCell("STEP COUNT: "));
//        data.add(new CharCell("ACTIVITY: "));
//        data.add(new CharCell("CADENCE: "));
//        data.get(0).setData("");
//
//        adapter = new CharAdapter(inflater, data);
//        recyclerView.setAdapter(adapter);
//
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        Field mDragger = null;//mRightDragger for right obviously
//        try {
//            mDragger = drawer.getClass().getDeclaredField("mLeftDragger");
//            mDragger.setAccessible(true);
//            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(drawer);
//
//            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
//            mEdgeSize.setAccessible(true);
//            int edge = mEdgeSize.getInt(draggerObj);
//            mEdgeSize.setInt(draggerObj, edge * 10); // вот тут меняем размер.
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_home, R.id.nav_settings, R.id.nav_account)
//                .setDrawerLayout(drawer)
//                .build();
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_device_list);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
//
//        db = new DatabaseHelper(this);
//        db.clear();
//
//        gatt = _device.connectGatt(this, true, mCallback);
//        mqtt();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_device_list, menu);
//        _menuItem = menu.findItem(R.id.scan);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.scan:
//                /*String selectQuery = "SELECT  * FROM " + Note.TABLE_NAME + " ORDER BY " +
//                        Note.COLUMN_TIMESTAMP + " DESC";
//
//                SQLiteDatabase db = this.db.getWritableDatabase();
//                Cursor cursor = db.rawQuery(selectQuery, null);
//
//                // looping through all rows and adding to list
//                if (cursor.moveToFirst()) {
//                    do {
//                        Note note = new Note();
//                        note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
//                        note.setNote(cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)));
//                        note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));
//
//                        send_data(mqttAndroidClient, note.getNote());
//                    } while (cursor.moveToNext());
//                }
//
//                // close db connection
//                db.close();
//                Toast.makeText(this.getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
//                 */
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        gatt.close();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private final BluetoothGattCallback mCallback = new BluetoothGattCallback() {
//
//        // Invoked when Bluetooth connection changes
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (newState == BluetoothProfile.STATE_CONNECTED) {
//                        //data.set(0, "Connected");
//                        data.get(0).setName("Connected");
//                        data.get(0).setData("");
//                        DeviceActivity.this.gatt.discoverServices();
//                    } else {
//                        //data.set(0, "Disconnected");
//                        data.get(0).setName("Disonnected");
//                        data.get(0).setData("");
//                        for (int i = 1; i <= 7; i++) {
//                            data.get(i).reset();
//                        }
//                        /*data.set(1, "HEART RATE: --");
//                        data.set(2, "RESP. RATE: --");
//                        data.set(3, "INSP: --");
//                        data.set(4, "EXP: --");
//                        data.set(5, "STEP COUNT: --");
//                        data.set(6, "ACTIVITY: --");
//                        data.set(7, "CADENCE: --");*/
//                    }
//                    // reload table view
//                    adapter.notifyDataSetChanged();
//                }
//            });
//
//        }
//
//        @Override
//        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                // Listen for Heart Rate notification
//                BluetoothGattService hrSvc = gatt.getService(HEART_RATE_MEASUREMENT_SERVICE_UUID);
//                BluetoothGattCharacteristic hrChar = hrSvc.getCharacteristic(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID);
//                gatt.setCharacteristicNotification(hrChar, true);
//                BluetoothGattDescriptor descriptor = hrChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                gatt.writeDescriptor(descriptor);
//            }
//        }
//
//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            if (descriptor.getCharacteristic().getUuid().equals(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
//                // Listen for Respiration Rate notification
//                BluetoothGattService respSvc = gatt.getService(RESPIRATION_SERVICE_UUID);
//                BluetoothGattCharacteristic respChar = respSvc.getCharacteristic(RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID);
//                gatt.setCharacteristicNotification(respChar, true);
//                BluetoothGattDescriptor respDescriptor = respChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
//                respDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                gatt.writeDescriptor(respDescriptor);
//            } else if (descriptor.getCharacteristic().getUuid().equals(RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
//                //Listen for Accelerometer notification
//                BluetoothGattService accSvc = gatt.getService(ACCELEROMETER_SERVICE_UUID);
//                BluetoothGattCharacteristic accChar = accSvc.getCharacteristic(ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID);
//                gatt.setCharacteristicNotification(accChar, true);
//                BluetoothGattDescriptor accDescriptor = accChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
//                accDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                gatt.writeDescriptor(accDescriptor);
//            }
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    byte[] data = characteristic.getValue();
//                    UUID uuid = characteristic.getUuid();
//                    String hexString = byteArrayToHex(data);
//
//                    //Heart Rate Received
//                    if (uuid.equals(HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
//                        int flag = data[0];
//                        int format;
//                        if ((flag & 0x01) == 0) {
//                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                        } else {
//                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                        }
//                        int heartRate = characteristic.getIntValue(format, 1);
//                        DeviceActivity.this.data.get(1).setData(String.valueOf(heartRate));
//                        //DeviceActivity.this.data.set(1, "HEART RATE: " + heartRate/* + ", (" + hexString + ")"*/);
//                        adapter.notifyDataSetChanged();
//
//                    }
//                    // Respiration Rate Received
//                    else if (uuid.equals(RESPIRATION_RATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
//
//                        byte flag = data[0];
//                        int format;
//                        if ((flag & 0x01) == 0) {
//                            format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                        } else {
//                            format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                        }
//
//                        int respRate = characteristic.getIntValue(format, 1);
//                        //DeviceActivity.this.data.set(2, "RESP. RATE: " + respRate/* + ", (" + hexString + ")"*/);
//                        DeviceActivity.this.data.get(2).setData(String.valueOf(respRate));
//
//                        boolean isInspExpPresent = (flag & 0x02) != 0;
//                        //DeviceActivity.this.data.set(3, "INSP: ");
//                        //DeviceActivity.this.data.set(4, "EXP: ");
//                        DeviceActivity.this.data.get(3).setData("");
//                        DeviceActivity.this.data.get(4).setData("");
//                        if (isInspExpPresent) {
//                            int startOffset = 1 + (format == BluetoothGattCharacteristic.FORMAT_UINT8 ? 1 : 2);
//                            boolean inspFirst = (flag & 0x04) == 0;
//                            StringBuilder sb_insp = new StringBuilder();
//                            StringBuilder sb_exp = new StringBuilder();
//                            //sb_insp.append("INSP: ");
//                            //sb_exp.append("INSP: ");
//                            for (int i = startOffset; i < data.length; i += 2) {
//                                float value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, i) / 32.0f;
//                                if (inspFirst) {
//                                    sb_insp.append(value);
//                                    inspFirst = false;
//                                } else {
//                                    sb_exp.append(value);
//                                    inspFirst = true;
//                                }
//                            }
//                            DeviceActivity.this.data.get(3).setData(sb_insp.toString());
//                            DeviceActivity.this.data.get(4).setData(sb_exp.toString());
//                            //DeviceActivity.this.data.set(3, sb_insp.toString());
//                            //DeviceActivity.this.data.set(4, sb_exp.toString());
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//
//                    // Accelerometer data received
//                    else if (uuid.equals((ACCELEROMETER_MEASUREMENT_CHARACTERISTIC_UUID))) {
//                        byte flag = data[0];
//                        int format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                        int dataIndex = 1;
//
//                        boolean isStepCountPresent = (flag & 0x01) != 0;
//                        boolean isActivityPresent = (flag & 0x02) != 0;
//                        boolean isCadencePresent = (flag & 0x04) != 0;
//
//                        if (isStepCountPresent) {
//                            int stepCount = characteristic.getIntValue(format, dataIndex);
//                            DeviceActivity.this.data.get(5).setData(String.valueOf(stepCount));
//                            //DeviceActivity.this.data.set(5, "STEP COUNT: " + stepCount/* + ", (" + hexString + ")"*/);
//                            dataIndex = dataIndex + 2;
//                        }
//
//                        if (isActivityPresent) {
//                            float activity = characteristic.getIntValue(format, dataIndex) / 256.0f;
//                            DeviceActivity.this.data.get(6).setData(String.valueOf(activity));
//                            //DeviceActivity.this.data.set(6, "ACTIVITY: " + activity/* + ", (" + hexString + ")"*/);
//                            dataIndex = dataIndex + 2;
//                        }
//
//                        if (isCadencePresent) {
//                            int cadence = characteristic.getIntValue(format, dataIndex);
//                            DeviceActivity.this.data.get(7).setData(String.valueOf(cadence));
//                            //DeviceActivity.this.data.set(7, "CADENCE: " + cadence/* + ", (" + hexString + ")"*/);
//                        }
//                    }
//                    DateFormat dfDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
//                    Date now = new Date();
//                    String mydate = dfDateAndTime.format(now);
//
//                    Map<String, String> result = new HashMap<>();
//                    try {
//                        result.put("HeartRate", DeviceActivity.this.data.get(1).getData());
//                        result.put("Steps", DeviceActivity.this.data.get(5).getData());
//                        result.put("Activity", DeviceActivity.this.data.get(6).getData());
//                        result.put("Cadence", DeviceActivity.this.data.get(7).getData());
//                        result.put("Clitime", mydate);
//                        result.put("RespRate", DeviceActivity.this.data.get(2).getData());
//                        result.put("Insp", DeviceActivity.this.data.get(3).getData());
//                        result.put("Exp", DeviceActivity.this.data.get(4).getData());
//                    } catch (Exception ex) {
//
//                    }
//
//                    String jsonString = JSON.toJSONString(result);
//                    Log.d(TAG, jsonString);
//                    //send_data(_mqttAndroidClient, jsonString);
//                    db.insertNote(result);
//                }
//            });
//        }
//    };
//
//    private static String byteArrayToHex(byte[] a) {
//        StringBuilder sb = new StringBuilder(a.length * 2);
//        for(byte b: a)
//            sb.append(String.format("%02x", b & 0xff));
//        return sb.toString();
//    }
//
//    public void send_data(final MqttAndroidClient mqttAndroidClient, final String data) {
//        try {
//            mqttAndroidClient.connect(null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.d(TAG, "Connection Success!");
//
//                    String id = "12345678";
//                    try {
//                        MqttMessage message = new MqttMessage(data.getBytes());
//                        Log.d(TAG, "Publishing message" + message);
//                        message.setQos(2);
//                        message.setRetained(false);
//                        mqttAndroidClient.publish("iomt/client/" + id + "/data", message);
//                    } catch (MqttException ex) {
//
//                    }
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.d(TAG, "Connection Failure!");
//                    Log.d(TAG, String.valueOf(exception));
//                }
//            });
//        } catch (MqttException ex) {
//
//        }
//    }
//
//    public void mqtt() {
//        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://test.mosquitto.org:1883", "androidSampleClient");
//        mqttAndroidClient.setCallback(new MqttCallback() {
//            @Override
//            public void connectionLost(Throwable cause) {
//                Log.d(TAG, "Connection was lost!");
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                Log.d(TAG, "Message Arrived!: " + topic + ": " + new String(message.getPayload()));
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken token) {
//                Log.d(TAG, "Delivery Complete!");
//            }
//        });
//    }
//}
package com.iomt.android;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Field;


public class DeviceActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private AppBarConfiguration mAppBarConfiguration;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_dev);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Field mDragger = null;
        try {
            mDragger = drawer.getClass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(drawer);

            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);
            mEdgeSize.setInt(draggerObj, edge * 5);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        NavigationView navigationView = findViewById(R.id.nav_view_dev);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_device, R.id.nav_settings, R.id.nav_account)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_device);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Context context = getApplicationContext();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_device);

        SharedPreferences prefs = getSharedPreferences(context.getString(R.string.ACC_DATA), MODE_PRIVATE);
        String sname = prefs.getString("name", "Null");
        String ssurname = prefs.getString("surname", "Null");
        int h = prefs.getInt("height", 0);
        int w = prefs.getInt("weight", 0);

        TextView surname =findViewById(R.id.surname_head);
        surname.setText(ssurname);

        TextView name =findViewById(R.id.name_head);
        name.setText(sname);

        TextView height =findViewById(R.id.height_head);
        height.setText(String.format(context.getString(R.string.height_head), h));

        TextView weight =findViewById(R.id.weight_head);
        weight.setText(String.format(context.getString(R.string.weight_head), w));

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
