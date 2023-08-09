/**
 * File containing whole Drawer
 */

package com.iomt.android.compose.components.drawer

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.iomt.android.compose.theme.colorScheme
import kotlinx.coroutines.launch

/**
 * @param navController main [NavHostController]
 * @param content content of a drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.S)
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

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@Preview
@Composable
private fun DrawerPreview() {
    val navController = rememberNavController()
    MaterialTheme(colorScheme) { Drawer(navController) { } }
}
