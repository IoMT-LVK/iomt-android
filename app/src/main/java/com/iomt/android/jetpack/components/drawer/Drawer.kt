/**
 * File containing whole Drawer
 */

package com.iomt.android.jetpack.components.drawer

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

/**
 * @param navController
 * @param content
 */
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Composable
fun Drawer(navController: NavHostController, content: @Composable (() -> Unit) -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader()
                DrawerBody(navController) { scope.launch { drawerState.close() } }
            }
        },
        drawerState = drawerState,
        gesturesEnabled = true,
    ) { content { scope.launch { drawerState.open() } } }
}
