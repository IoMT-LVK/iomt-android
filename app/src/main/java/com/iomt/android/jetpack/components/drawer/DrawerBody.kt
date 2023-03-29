/**
 * File containing Drawer body
 */

package com.iomt.android.jetpack.components.drawer

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.iomt.android.jetpack.components.NavRouter

/**
 * @param navController
 * @param closeNavDrawer
 */
@Composable
internal fun DrawerBody(navController: NavHostController?, closeNavDrawer: () -> Unit) {
    listOf(NavRouter.HOME, NavRouter.ACCOUNT, NavRouter.SETTINGS)
        .map { route ->
            DrawerMenuItem(
                iconDrawableId = route.iconId,
                text = route.path,
                isSelected = navController?.currentDestination?.route == route.path,
                onItemClick = {
                    navController?.navigate(route.path)
                    closeNavDrawer()
                }
            )
        }
}
