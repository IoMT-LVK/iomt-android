/**
 * File containing Drawer body
 */

package com.iomt.android.jetpack.components.drawer

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.iomt.android.jetpack.navigation.NavRouter
import com.iomt.android.utils.navigate

/**
 * @param navController
 * @param closeNavDrawer
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
            }
        )
    }
}
