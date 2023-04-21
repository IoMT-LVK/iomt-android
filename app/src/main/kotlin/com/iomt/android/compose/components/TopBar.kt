/**
 * Top bar component
 */

package com.iomt.android.compose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.iomt.android.compose.theme.colorScheme

/**
 * @param navController main [NavController]
 * @param onMenuButtonClicked callback to change the drawer state
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
        title = { Text(currentRoute.split("/").first()) },
        navigationIcon = {
            IconButton(onClick = onMenuButtonClicked) {
                Icon(Icons.Default.Menu, "Navigation")
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(colorScheme.primaryContainer),
    )
}

@Preview
@Composable
private fun TopBarPreview() {
    val navController = rememberNavController()
    MaterialTheme(colorScheme) { TopBar(navController) { } }
}
