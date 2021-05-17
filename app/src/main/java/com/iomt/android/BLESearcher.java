package com.iomt.android;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BLESearcher extends AppCompatActivity implements SavedDeviceAdapter.OnClickListener {
    private Handler uiHandler;

    private MenuItem menuItem;

    private List<DeviceCell> deviceCells = new ArrayList<>();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private SavedDeviceAdapter myAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private List<DeviceType> dev_types = new ArrayList<>();
    private List<DeviceInfo> devs = new ArrayList<>();
    private HTTPRequests httpRequests;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searcher);


        String JWT = getIntent().getStringExtra("JWT");
        String userId = getIntent().getStringExtra("UserId");
        httpRequests = new HTTPRequests(this, JWT, userId);
        Action action_types = (String[] args) -> {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Type listOfDeviceTypes = new TypeToken<ArrayList<DeviceType>>(){}.getType();
            dev_types = gson.fromJson(args[0], listOfDeviceTypes);
        };
        httpRequests.getDeviceTypes(action_types);

        Action action_devs = (String[] args) -> {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Type listOfDevices= new TypeToken<ArrayList<DeviceInfo>>(){}.getType();
            devs = gson.fromJson(args[0], listOfDevices);
        };
        httpRequests.getDevices(action_devs);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        uiHandler = new Handler(Looper.getMainLooper());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_searcher);
        setSupportActionBar(toolbar);


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.searcher_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter = new SavedDeviceAdapter(LayoutInflater.from(this), deviceCells, this);
        recyclerView.setAdapter(myAdapter);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    private String get_type(BluetoothDevice d) {
        for (DeviceType i: dev_types) {
            if (d.getName().startsWith(i.getPrefix())) {
                return i.getDevice_type();
            }
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                menuItem.setEnabled(true);
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location not granted");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover nearby bluetooth devices.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();

                menuItem.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_searcher, menu);
        menuItem = menu.findItem(R.id.scan);
        return true;
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.scan) {
            scanLeDevice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    if (result.getDevice().getName() != null) {
                        Log.d("FOUND DEVICE", result.getDevice().getName());
                        String type = get_type(result.getDevice());
                        if (type != null) {
                            boolean found = false;
                            for (DeviceCell d : deviceCells) {
                                if (d.getDevice().getName().equals(result.getDevice().getName())) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                Log.d("ADD DEVICE", result.getDevice().getName());
                                deviceCells.add(new DeviceCell(result.getDevice()));
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            };
    private static final long SCAN_PERIOD = 10000;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scanLeDevice() {
        deviceCells.clear();
        myAdapter.notifyDataSetChanged();
        uiHandler.postDelayed(this::stopScan, SCAN_PERIOD);
        menuItem.setEnabled(false);
        mBluetoothLeScanner.startScan(mLeScanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScan() {
        menuItem.setEnabled(true);
        mBluetoothLeScanner.stopScan(mLeScanCallback);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onClickItem(DeviceCell deviceCell, BluetoothDevice device) {
        stopScan();

        DeviceInfo deviceInfo = new DeviceInfo(device, get_type(device));
        boolean found = false;
        for (DeviceInfo i: devs) {
            if (i.equals(deviceInfo)) {
                found = true;
                break;
            }
        }
        if (!found) {
            httpRequests.send_dev(deviceInfo);
        }

        finish();
    }
}
