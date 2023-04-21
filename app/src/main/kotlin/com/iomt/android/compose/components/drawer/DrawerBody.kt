/**
 * File containing Drawer body
 */

package com.iomt.android.compose.components.drawer

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.iomt.android.compose.navigation.NavRouter
import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.utils.navigate

/**
 * @param navController main [NavHostController]
 * @param closeNavDrawer callback to close drawer
 */
@Composable
internal fun DrawerBody(navController: NavHostController?, closeNavDrawer: () -> Unit) {
    NavRouter.Main.drawerList.map { route ->
        DrawerMenuItem(
            iconDrawableId = route.iconId,
            text = route.path,
            isSelected = navController?.currentDestination?.route == route.path,
            onItemClick = {
                navController?.navigate(route)
                closeNavDrawer()
            },
        )
    }
}

@Preview
@Composable
private fun DrawerBodyPreview() {
    val navController = rememberNavController()
    MaterialTheme(colorScheme) { DrawerBody(navController) { } }
}
