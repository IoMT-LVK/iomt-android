/**
 * Utils for Composable functions
 */

package com.iomt.android.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.iomt.android.jetpack.navigation.NavRouter

/**
 * Utility method to enable constructions like
 *
 * val bluetoothManager: BluetoothManager = LocalContext.getService()
 */
@Composable
inline fun <reified T : Any> ProvidableCompositionLocal<Context>.getService(): T = requireNotNull(
    current.getSystemService(T::class.java)
) { "Could not get ${T::class.java.simpleName}" }

/**
 * @param navRouter
 * @param arguments
 * @param deepLinks
 * @param content
 */
fun NavGraphBuilder.composable(
    navRouter: NavRouter,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit,
): Unit = composable(navRouter.path, arguments, deepLinks, content)

/**
 * @param navRouter
 * @param navOptions
 * @param navigatorExtras
 */
fun NavHostController.navigate(
    navRouter: NavRouter,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
): Unit = navigate(navRouter.path, navOptions, navigatorExtras)
