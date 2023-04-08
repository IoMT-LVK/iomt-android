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
 * @param navRouter route for the destination
 * @param arguments list of arguments to associate with destination
 * @param deepLinks list of deep links to associate with the destinations
 * @param content [Composable] for the destination
 */
fun NavGraphBuilder.composable(
    navRouter: NavRouter,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit,
): Unit = composable(navRouter.path, arguments, deepLinks, content)

/**
 * @param navRouter route for the destination
 * @param navOptions special options for this navigation operation
 * @param navigatorExtras extras to pass to the Navigator
 * @throws IllegalArgumentException - if the given route is invalid
 */
fun NavHostController.navigate(
    navRouter: NavRouter,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
): Unit = navigate(navRouter.path, navOptions, navigatorExtras)
