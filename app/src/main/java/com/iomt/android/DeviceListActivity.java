package com.iomt.android;

import android.Manifest;
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

/**
 * Displays list of Hexoskin Devices
 */
public class DeviceListActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private AppBarConfiguration mAppBarConfiguration;
    private String sname;
    private String ssurname;
    private String JWT, UserId;
    private int h;
    private int w;
    private HTTPRequests httpRequests;
    private Action action;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);

        JWT = getIntent().getStringExtra("JWT");
        UserId = getIntent().getStringExtra("UserId");
        httpRequests = new HTTPRequests(this, JWT, UserId);

        action = (String[] args) -> {
            w = Integer.parseInt(args[0]);
            h = Integer.parseInt(args[1]);
            sname = args[5];
            ssurname = args[6];

            SharedPreferences.Editor editor = getSharedPreferences(getApplicationContext().getString(R.string.ACC_DATA), MODE_PRIVATE).edit();
            editor.putString("name", sname);
            editor.putString("surname", ssurname);
            editor.putInt("height", h);
            editor.putInt("weight", w);
            editor.putString("JWT", JWT);
            editor.putString("UserId", UserId);
            editor.apply();


            TextView surname = findViewById(R.id.surname_head);
            surname.setText(ssurname);

            TextView name = findViewById(R.id.name_head);
            name.setText(sname);

            TextView height = findViewById(R.id.height_head);
            height.setText(String.format(getApplicationContext().getString(R.string.height_head), h));

            TextView weight = findViewById(R.id.weight_head);
            weight.setText(String.format(getApplicationContext().getString(R.string.weight_head), w));
        };

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Field mDragger;
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
        NavigationView navigationView = findViewById(R.id.nav_view_dlist);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings, R.id.nav_account)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_device_list);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_device_list);

        httpRequests.getData(action);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        httpRequests.getData(action);
        super.onResume();
    }
}
