package com.iomt.android

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI

import com.iomt.android.entities.UserData

import com.google.android.material.navigation.NavigationView

import java.lang.reflect.Field

/**
 * Displays list of Hexoskin Devices
 */
class DeviceListActivity : AppCompatActivity() {
    private var appBarConfiguration: AppBarConfiguration? = null
    private var jwt: String? = null
    private var userId: String? = null
    private var httpRequests: Requests? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        val toolbar = findViewById<View>(R.id.toolbar_list) as Toolbar
        setSupportActionBar(toolbar)
        jwt = intent.getStringExtra("JWT")
        userId = intent.getStringExtra("UserId")
        httpRequests = Requests(jwt, userId)
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
        val navigationView: NavigationView = findViewById(R.id.nav_view_dlist)
        appBarConfiguration = AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_settings, R.id.nav_account).setOpenableLayout(drawer).build()
        val navController = Navigation.findNavController(this, R.id.nav_host_device_list)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView, navController)
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_FINE_LOCATION
        )
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA")
    private val getDataRequestCallback: (UserData) -> Unit = { userData ->
        getSharedPreferences(applicationContext.getString(R.string.ACC_DATA), MODE_PRIVATE).edit().apply {
            putString("name", userData.name)
            putString("surname", userData.surname)
            putInt("height", userData.height)
            putInt("weight", userData.weight)
            putString("JWT", jwt)
            putString("UserId", userId)
        }.apply()
        findViewById<TextView>(R.id.name_head).apply { text = userData.name }
        findViewById<TextView>(R.id.surname_head).apply { text = userData.surname }
        findViewById<TextView>(R.id.height_head).apply { text = String.format(applicationContext.getString(R.string.height_head), userData.height) }
        findViewById<TextView>(R.id.weight_head).apply { text = String.format(applicationContext.getString(R.string.weight_head), userData.weight) }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_device_list)
        httpRequests!!.getData(getDataRequestCallback)
        return (NavigationUI.navigateUp(navController, appBarConfiguration!!) || super.onSupportNavigateUp())
    }

    override fun onResume() = httpRequests?.getData(getDataRequestCallback).also { super.onResume() } ?: Unit

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }
}
