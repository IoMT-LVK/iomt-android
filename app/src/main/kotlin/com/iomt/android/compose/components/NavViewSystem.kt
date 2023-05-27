/**
 * Entry point on an app after login was successful
 */

package com.iomt.android.compose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.iomt.android.compose.Scaffold
import com.iomt.android.compose.components.drawer.Drawer
import com.iomt.android.compose.theme.colorScheme

/**
 * @param signOut callback to sign out
 */
@Composable
fun NavViewSystemWithDrawer(signOut: () -> Unit) {
    val navController = rememberNavController()
    Drawer(navController) { openDrawer ->
        Scaffold(navController, signOut, openDrawer)
    }
}

@Preview
@Composable
private fun NavViewSystemWithDrawerPreview() {
    MaterialTheme(colorScheme) { NavViewSystemWithDrawer { } }
}
