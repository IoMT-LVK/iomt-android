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

/**
 * Displays list of Hexoskin Devices
 */
class DeviceListActivity : AppCompatActivity() {
    private var mAppBarConfiguration: AppBarConfiguration? = null
    private var sname: String? = null
    private var ssurname: String? = null
    private var jwt: String? = null
    private var userId: String? = null
    private var h = 0
    private var w = 0
    private var httpRequests: HTTPRequests? = null
    private var action: Action? = null

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        val toolbar = findViewById<View>(R.id.toolbar_list) as Toolbar
        setSupportActionBar(toolbar)
        jwt = intent.getStringExtra("JWT")
        userId = intent.getStringExtra("UserId")
        httpRequests = HTTPRequests(this, jwt, userId)
        action = Action { args: Array<String?>? ->
            w = args!![0]!!.toInt()
            h = args[1]!!.toInt()
            sname = args[5]
            ssurname = args[6]
            val editor = getSharedPreferences(
                applicationContext.getString(R.string.ACC_DATA),
                MODE_PRIVATE
            ).edit()
            editor.putString("name", sname)
            editor.putString("surname", ssurname)
            editor.putInt("height", h)
            editor.putInt("weight", w)
            editor.putString("JWT", jwt)
            editor.putString("UserId", userId)
            editor.apply()
            val surname = findViewById<TextView>(R.id.surname_head)
            surname.text = ssurname
            val name = findViewById<TextView>(R.id.name_head)
            name.text = sname
            val height = findViewById<TextView>(R.id.height_head)
            height.text = String.format(applicationContext.getString(R.string.height_head), h)
            val weight = findViewById<TextView>(R.id.weight_head)
            weight.text = String.format(applicationContext.getString(R.string.weight_head), w)
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val mDragger: Field
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
        val navigationView = findViewById<NavigationView>(R.id.nav_view_dlist)
        mAppBarConfiguration = AppBarConfiguration
            .Builder(R.id.nav_home, R.id.nav_settings, R.id.nav_account)
            .setOpenableLayout(drawer)
            .build()
        val navController = Navigation.findNavController(this, R.id.nav_host_device_list)
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
        val navController = Navigation.findNavController(this, R.id.nav_host_device_list)
        httpRequests!!.getData(action!!)
        return (NavigationUI.navigateUp(navController, mAppBarConfiguration!!) || super.onSupportNavigateUp())
    }

    override fun onResume() {
        httpRequests!!.getData(action!!)
        super.onResume()
    }

    companion object {
        private const val PERMISSION_REQUEST_FINE_LOCATION = 1
    }
}