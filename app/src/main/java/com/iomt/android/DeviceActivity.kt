package com.iomt.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import java.lang.reflect.Field

class DeviceActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        val toolbar = findViewById<View>(R.id.toolbar_dev) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        var mDragger: Field? = null
        try {
            mDragger = drawer.javaClass.getDeclaredField("mLeftDragger")
            mDragger.isAccessible = true
            val draggerObj = mDragger[drawer] as ViewDragHelper
            val mEdgeSize = draggerObj.javaClass.getDeclaredField("mEdgeSize")
            mEdgeSize.isAccessible = true
            val edge = mEdgeSize.getInt(draggerObj)
            mEdgeSize.setInt(draggerObj, edge * 5)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val navigationView = findViewById<NavigationView>(R.id.nav_view_dev)
        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_device, R.id.nav_settings, R.id.nav_account
        )
            .setDrawerLayout(drawer)
            .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_device)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val context = applicationContext
        val navController = Navigation.findNavController(this, R.id.nav_host_device)
        val prefs = getSharedPreferences(context.getString(R.string.ACC_DATA), MODE_PRIVATE)
        val sname = prefs.getString("name", "Null")
        val ssurname = prefs.getString("surname", "Null")
        val h = prefs.getInt("height", 0)
        val w = prefs.getInt("weight", 0)
        val surname = findViewById<TextView>(R.id.surname_head)
        surname.text = ssurname
        val name = findViewById<TextView>(R.id.name_head)
        name.text = sname
        val height = findViewById<TextView>(R.id.height_head)
        height.text = String.format(context.getString(R.string.height_head), h)
        val weight = findViewById<TextView>(R.id.weight_head)
        weight.text = String.format(context.getString(R.string.weight_head), w)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration!!)
                || super.onSupportNavigateUp())
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }
}