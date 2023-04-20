/**
 * File containing the scaffold of post-login part of app
 */

package com.iomt.android.jetpack

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.iomt.android.jetpack.components.TopBar
import com.iomt.android.jetpack.navigation.NavRouter
import com.iomt.android.jetpack.navigation.NavRouter.Companion.useMainNavHost
import com.iomt.android.utils.FloatingButtonBuilder

/**
 * @param navController post-login [NavHostController]
 * @param signOut callback invoked on sign out
 * @param onMenuButtonPressed callback invoked on menu button pressed - this should open drawer
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
fun Scaffold(
    navController: NavHostController,
    signOut: () -> Unit,
    onMenuButtonPressed: () -> Unit,
) {
    val mutableFloatingActionButtonBuilder = remember { mutableStateOf<FloatingButtonBuilder>({ }) }

    Scaffold(
        Modifier.fillMaxSize(),
        topBar = { TopBar(navController, onMenuButtonClicked = onMenuButtonPressed) },
        floatingActionButton = { mutableFloatingActionButtonBuilder.value(navController) },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        navController.useMainNavHost(
            Modifier.padding(paddingValues),
            mutableFloatingActionButtonBuilder,
            signOut,
        ) { navController.navigate("${NavRouter.Main.Device}/${it.address}") }
    }
}
