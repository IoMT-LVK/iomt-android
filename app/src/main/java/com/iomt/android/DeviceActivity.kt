package com.iomt.android

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import java.lang.reflect.Field

/**
 * [AppCompatActivity]
 */
class DeviceActivity : AppCompatActivity() {
    private var appBarConfiguration: AppBarConfiguration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        val toolbar: Toolbar = findViewById(R.id.toolbar_dev)
        setSupportActionBar(toolbar)
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val dragger: Field
        try {
            dragger = drawer.javaClass.getDeclaredField("mLeftDragger")
            dragger.isAccessible = true
            val draggerObj = dragger[drawer] as ViewDragHelper
            val edgeSize = draggerObj.javaClass.getDeclaredField("mEdgeSize").apply {
                isAccessible = true
            }
            val edge = edgeSize.getInt(draggerObj)
            edgeSize.setInt(draggerObj, edge * 5)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val navigationView: NavigationView = findViewById(R.id.nav_view_dev)
        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_device, R.id.nav_settings, R.id.nav_account
        )
            .setDrawerLayout(drawer)
            .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_device)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController)
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_FINE_LOCATION
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val context = applicationContext
        val navController = Navigation.findNavController(this, R.id.nav_host_device)
        val prefs = getSharedPreferences(context.getString(R.string.ACC_DATA), MODE_PRIVATE)
        val name = prefs.getString("name", "Null")
        val surname = prefs.getString("surname", "Null")
        val height = prefs.getInt("height", 0)
        val weight = prefs.getInt("weight", 0)
        findViewById<TextView>(R.id.name_head).apply {
            text = name
        }
        findViewById<TextView>(R.id.surname_head).apply {
            text = surname
        }
        findViewById<TextView>(R.id.height_head).apply {
            text = String.format(context.getString(R.string.height_head), height)
        }
        findViewById<TextView>(R.id.weight_head).apply {
            text = String.format(context.getString(R.string.weight_head), weight)
        }
        return (NavigationUI.navigateUp(navController, appBarConfiguration!!) || super.onSupportNavigateUp())
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }
}
