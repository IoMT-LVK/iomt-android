//package com.iomt.android.ui.home;
//
//import android.arch.lifecycle.ViewModelProviders;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//
//import com.iomt.android.DeviceAdapter;
//import com.iomt.android.DeviceCell;
//import com.iomt.android.R;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//
//public class HomeFragment extends Fragment implements DeviceAdapter.OnClickListener {
//
//    private HomeViewModel homeViewModel;
//    private RecyclerView.Adapter myAdapter;
//    private Handler uiHandler;
//    private BluetoothAdapter bluetoothAdapter;
//    private MenuItem menuItem;
//    private List<DeviceCell> deviceCells = new ArrayList<>();
//    private LayoutInflater inflater;
//    private RecyclerView recyclerView;
//    private Context context;
//
////    public View onCreateView(@NonNull LayoutInflater inflater,
////                             ViewGroup container, Bundle savedInstanceState) {
////        homeViewModel =
////                ViewModelProviders.of(this).get(HomeViewModel.class);
////
////        return inflater.inflate(R.layout.fragment_home, container, false);
////    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
//        uiHandler = new Handler();
//        bluetoothAdapter = ((BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
//        context = requireContext();
//        inflater = LayoutInflater.from(context);
//    }
//
////    @Override
////    public View onCreateView(@NonNull LayoutInflater inflater,
////                             ViewGroup container, Bundle savedInstanceState) {
////
////        View view = inflater.inflate(R.layout.fragment_home, container, false);
////        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_list);
////        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
////
////        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
////
////        recyclerView = Objects.requireNonNull(view).findViewById(R.id.my_recycler_view);
////        recyclerView.setHasFixedSize(true);
////        RecyclerView.LayoutManager _layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
////        recyclerView.setLayoutManager(_layoutManager);
////        myAdapter = new DeviceAdapter(inflater, deviceCells, this);
////        recyclerView.setAdapter(myAdapter);
////        return view;
////    }
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        recyclerView = view.findViewById(R.id.my_recycler_view);
//        recyclerView.setHasFixedSize(true);
//
//        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_list);
//        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
//
//        RecyclerView.LayoutManager _layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
//        recyclerView.setLayoutManager(_layoutManager);
//        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
//
//        //myAdapter = recyclerView.getAdapter();
//
//
//        myAdapter = new DeviceAdapter(inflater, deviceCells, this);
//        recyclerView.setAdapter(myAdapter);
//    }
//
//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi,
//                                     byte[] scanRecord) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (device.getName() != null && device.getName().startsWith("HX")) {
//                                Log.d("FOUND DEVICE", device.getName());
//                                boolean found = false;
//                                for (DeviceCell d : deviceCells) {
//                                    if (d.getDevice().getName().equals(device.getName())) {
//                                        found = true;
//                                        break;
//                                    }
//                                }
//
//                                if (!found) {
//                                    Log.d("ADD DEVICE", device.getName());
//                                    deviceCells.add(new DeviceCell(device));
//                                    myAdapter.notifyDataSetChanged();
//                                }
//                            }
//                        }
//                    });
//                }
//            };
//
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        //MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_device_list, menu);
//        super.onCreateOptionsMenu(menu,inflater);
//        menuItem = menu.findItem(R.id.scan);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.scan:
//                if (myAdapter == null) {
//                    myAdapter = new DeviceAdapter(inflater, deviceCells, this);
//                    if (recyclerView != null && recyclerView.getAdapter() != myAdapter)
//                        recyclerView.setAdapter(myAdapter);
//                }
//                scanLeDevice();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 10000;
//
//    // Scan for BLE devices
//    private void scanLeDevice() {
//        // Clear devices
//        deviceCells.clear();
//        //_devices.clear();
//
//        // reload table
//        myAdapter.notifyDataSetChanged();
//        //stopScan();
//
//        // Stops scanning after a pre-defined scan period.
//        uiHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stopScan();
//            }
//        }, SCAN_PERIOD);
//
//        menuItem.setEnabled(false);
//
//        // scan
//        bluetoothAdapter.startLeScan(mLeScanCallback);
//    }
//
//    public void stopScan() {
//        menuItem.setEnabled(true);
//        bluetoothAdapter.stopLeScan(mLeScanCallback);
//    }
//
//    @Override
//    public void onClickItem(DeviceCell deviceCell, BluetoothDevice device) {
//        stopScan();
//        // Show Device Detail
//        Intent intent = new Intent(getActivity(), HomeFragment.class);
//        intent.putExtra("Device", device);
//        startActivity(intent);
//    }
//}


package com.iomt.android.ui.home;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.iomt.android.Action;
import com.iomt.android.DeviceActivity;
import com.iomt.android.DeviceAdapter;
import com.iomt.android.DeviceCell;
import com.iomt.android.DeviceInfo;
import com.iomt.android.HTTPRequests;
import com.iomt.android.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Displays list of Hexoskin Devices
 */
public class HomeFragment extends Fragment implements DeviceAdapter.OnClickListener {
    private Handler uiHandler;

    private MenuItem menuItem;

    private List<DeviceCell> deviceCells = new ArrayList<>();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private DeviceAdapter myAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private String JWT;
    private String UserId;
    private HTTPRequests httpRequests;
    private List<DeviceInfo> deviceInfos = new ArrayList<>();
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = builder.create();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (bluetoothAdapter == null) {
            Toast.makeText(this.requireContext(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        if (ContextCompat.checkSelfPermission(this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this.requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        statusCheck();

        mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        uiHandler = new Handler(Looper.getMainLooper());

        SharedPreferences prefs = requireActivity().getSharedPreferences(requireContext().getString(R.string.ACC_DATA), MODE_PRIVATE);
        JWT = prefs.getString("JWT", "");
        UserId = prefs.getString("UserId", "");

        httpRequests = new HTTPRequests(requireContext(), JWT, UserId);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Your GPS seems to be disabled, but it is needed for Bluetooth correct work")
                .setCancelable(false)
                .setPositiveButton("Enable", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Cancel", (dialog, id) -> {
                    dialog.cancel();
                    requireActivity().finish();
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            requireActivity().finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_list);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Устройства");

        RecyclerView recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        myAdapter = new DeviceAdapter(inflater, deviceCells, this);
        recyclerView.setAdapter(myAdapter);
        TextView no_dev = view.findViewById(R.id.text_no_dev);
        no_dev.setVisibility(View.GONE);

        Action action_cells = (String[] args) -> {
            Type listOfDevices = new TypeToken<ArrayList<DeviceInfo>>() {
            }.getType();
            deviceInfos = gson.fromJson(args[0], listOfDevices);
            no_dev.setVisibility(View.GONE);
            if (deviceInfos.size() == 0) {
                no_dev.setVisibility(View.VISIBLE);
            }
        };

        httpRequests.getDevices(action_cells);

        return view;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Location not granted");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover nearby bluetooth devices.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_device_list, menu);
        menuItem = menu.findItem(R.id.scan);
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

//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private BluetoothAdapter.LeScanCallback mLeScanCallback1 =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi,
//                                     byte[] scanRecord) {
//                    requireActivity().runOnUiThread(() -> {
//                        Log.d("BT", device.getName());
//                        if (device.getName() != null && device.getName().startsWith("HX")) {
//                            boolean found = false;
//                            for (DeviceCell d : deviceCells) {
//                                if (d.getDevice().getName().equals(device.getName())) {
//                                    found = true;
//                                    break;
//                                }
//                            }
//
//                            if(!found) {
//                                deviceCells.add(new DeviceCell(device));
//                                myAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    });
//                }
//            };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mLeScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    if (result.getDevice().getName() != null) {
                        Log.d("FOUND DEVICE", result.getDevice().getName());
                        boolean found = false;
                        for (DeviceInfo dev: deviceInfos) {
                            if (dev.getName().equals(result.getDevice().getName()) && dev.getAddress().equals(result.getDevice().getAddress())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            return;
                        }
                        found = false;
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
            };
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    // Scan for BLE devices
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scanLeDevice() {
        // Clear devices
        deviceCells.clear();

        // reload table
        myAdapter.notifyDataSetChanged();
        //stopScan();

        // Stops scanning after a pre-defined scan period.
        uiHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        menuItem.setEnabled(false);

        // scan
        //bluetoothAdapter.startLeScan(mLeScanCallback);
        mBluetoothLeScanner.startScan(mLeScanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScan() {
        menuItem.setEnabled(true);
        //bluetoothAdapter.stopLeScan(mLeScanCallback);
        mBluetoothLeScanner.stopScan(mLeScanCallback);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onClickItem(DeviceCell deviceCell, BluetoothDevice device) {
        stopScan();
        // Show Device Detail
        Intent intent = new Intent(getActivity(), DeviceActivity.class);
        intent.putExtra("Device", device);
        startActivity(intent);
    }
}
