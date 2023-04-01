/**
 * Entry point on an app after login was successful
 */

package com.iomt.android.jetpack.components

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.iomt.android.entities.AuthInfo
import com.iomt.android.jetpack.components.drawer.Drawer
import com.iomt.android.jetpack.theme.colorScheme

/**
 * @param authInfo current [AuthInfo] required for HTTP requests
 * @param signOut callback to sign out
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Composable
fun NavViewSystemWithDrawer(authInfo: AuthInfo, signOut: () -> Unit) {
    /* TODO: implement credential saving */

    val navController = rememberNavController()
    Drawer(navController) { openDrawer ->
        Scaffold(navController, authInfo, signOut, openDrawer)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Preview
@Composable
private fun NavViewSystemWithDrawerPreview() {
    MaterialTheme(colorScheme) { NavViewSystemWithDrawer(AuthInfo.empty) { } }
}
