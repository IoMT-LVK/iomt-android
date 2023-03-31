package com.iomt.android.jetpack.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import com.iomt.android.R
import com.iomt.android.entities.AuthInfo
import com.iomt.android.jetpack.view.*
import com.iomt.android.jetpack.view.login.EmailConfView
import com.iomt.android.jetpack.view.login.LoginView
import com.iomt.android.jetpack.view.main.AccountView
import com.iomt.android.jetpack.view.main.BleScannerView
import com.iomt.android.jetpack.view.main.HomeView
import com.iomt.android.jetpack.view.main.SettingsView
import com.iomt.android.utils.composable
import com.iomt.android.utils.navigate

/**
 * @property iconId id if icon that should be displayed
 * @property path path to view
 */
sealed class NavRouter(open val iconId: Int, open val path: String) {
    /**
     * Represents post-login part of the app
     * @property iconId
     * @property path
     */
    sealed class Main(override val iconId: Int, override val path: String) : NavRouter(iconId, path) {
        /**
         * Represents [AccountView] route
         */
        object Account : NavRouter(R.drawable.ic_menu_account, "Account")

        /**
         * Represents [BleScannerView] route
         */
        object BleScanner : NavRouter(R.drawable.blt, "Ble Scanner")

        /**
         * Represents [DeviceView] route
         */
        object Device : NavRouter(R.drawable.default_device, "Device {id}")

        /**
         * Represents [HomeView] route
         */
        object Home : NavRouter(R.drawable.ic_menu_home, "Home")

        /**
         * Represents [SettingsView] route
         */
        object Settings : NavRouter(R.drawable.ic_menu_settings, "Settings")
        companion object {
            /**
             * Represents default view of [NavRouter.Main] navigation subgraph - [HomeView]
             */
            val default = Home

            /**
             * List of routes that should be displayed in drawer
             */
            val drawerList = listOf(Home, Account, Settings)
        }
    }

    /**
     * Represents pre-login part of the app
     * @property iconId
     * @property path
     */
    sealed class Login(override val iconId: Int, override val path: String) : NavRouter(iconId, path) {
        /**
         * Represents [LoginView] route
         */
        object Login : NavRouter(-1, "Sign in")

        /**
         * Represents [RegisterView] route
         */
        object Register : NavRouter(-1, "Sign up")

        /**
         * Represents [EmailConfView] route
         */
        object EmailConf : NavRouter(-1, "Confirm your email")

        /**
         * Represents [NavRouter.Main] route - post-login part of the app
         */
        object Main : NavRouter(R.drawable.ic_menu_home, "Home")
        companion object {
            val default = Login
        }
    }
    companion object {
        /**
         * @param onAuthSuccess callback that should be invoked on successful login
         * @param modifier [Modifier] applied to [NavHost]
         * @param navViewSystemWithDrawer lambda that creates NavViewSystemWithDrawer for later app usage
         */
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
        @Composable
        @SuppressLint("ComposableNaming")
        fun NavHostController.useLoginNavHost(
            modifier: Modifier = Modifier,
            onAuthSuccess: (AuthInfo) -> Unit,
            navViewSystemWithDrawer: @Composable () -> Unit,
        ) {
            NavHost(this, modifier = modifier, startDestination = Login.default.path) {
                composable(Login.Login) { LoginView(onAuthSuccess, { navigate(Main.default) }) { navigate(Login.EmailConf) } }
                composable(Login.EmailConf) { EmailConfView { navigate(Login.Login) } }
                composable(Login.Register) { /* TODO: RegistrationView() */ }
                composable(Login.Main) { navViewSystemWithDrawer() }
            }
        }

        /**
         * @param knownDevices [SnapshotStateList] of known [BluetoothDevice]
         * @param signOut callback to sign out
         * @param onHomeDeviceClick callback invoked when [BluetoothDevice] was selected on [HomeView]
         * @param modifier [Modifier] applied to [NavHost]
         * @param onScannerDeviceClick callback invoked when [BluetoothDevice] was selected on [BleScannerView]
         */
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
        @Composable
        @SuppressLint("ComposableNaming")
        fun NavHostController.useMainNavHost(
            knownDevices: SnapshotStateList<BluetoothDevice>,
            modifier: Modifier = Modifier,
            signOut: () -> Unit,
            onHomeDeviceClick: (BluetoothDevice) -> Unit,
            onScannerDeviceClick: (BluetoothDevice) -> Unit,
        ) {
            NavHost(this, modifier = modifier, startDestination = Main.default.path) {
                composable(Main.Home) { HomeView(knownDevices, onHomeDeviceClick) }
                composable(Main.Settings) { SettingsView(signOut) }
                composable(Main.Device) { /* TODO: DeviceView() */ }
                composable(Main.Account) { AccountView(knownDevices) }
                composable(Main.BleScanner) { BleScannerView({ popBackStack() }, onScannerDeviceClick) }
            }
        }
    }
}
