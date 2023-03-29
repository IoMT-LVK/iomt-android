/**
 * Top bar component
 */

package com.iomt.android.jetpack.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * @param navController
 * @param onMenuButtonClicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    onMenuButtonClicked: () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: return

    TopAppBar(
        title = { Text(currentRoute) },
        navigationIcon = {
            IconButton(onClick = onMenuButtonClicked) {
                Icon(Icons.Default.Menu, "Navigation")
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(MaterialTheme.colorScheme.primaryContainer),
    )
}
