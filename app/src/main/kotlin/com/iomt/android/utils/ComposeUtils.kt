/**
 * Utils for Composable functions
 */

package com.iomt.android.utils

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable

import com.iomt.android.compose.navigation.NavRouter

import kotlin.time.Duration
import kotlinx.coroutines.*

typealias FloatingButtonBuilder = @Composable (NavHostController) -> Unit
typealias MutableFloatingButtonBuilder = MutableState<FloatingButtonBuilder>

/**
 * Utility method to enable constructions like
 *
 * val bluetoothManager: BluetoothManager = LocalContext.getService()
 */
@Composable
inline fun <reified T : Any> ProvidableCompositionLocal<Context>.getService(): T = requireNotNull(
    current.getSystemService(T::class.java),
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

/**
 * Shows [CircularProgressIndicator] if [value] is null, [content] otherwise
 *
 * @param value nullable value that defines the real rendered content
 * @param content content that should be displayed if [value] is not null
 */
@Composable
fun <T : Any> withLoading(value: T?, content: @Composable (T) -> Unit) {
    value?.let { content(value) } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

/**
 * @param delayDuration debounce period as [Duration]
 * @param scope [CoroutineScope] for async task processing
 * @param destinationFunction suspend function to debounce
 * @return [destinationFunction] wrapped with param value tracking for debouncing
 */
fun <T : Any> withDebounce(
    delayDuration: Duration,
    scope: CoroutineScope,
    destinationFunction: suspend (T) -> Unit,
): (T) -> Unit {
    var throttleJob: Job? = null
    var latestParam: T
    return { param: T ->
        latestParam = param
        if (throttleJob?.isCompleted != false) {
            throttleJob = scope.launch {
                delay(delayDuration)
                destinationFunction(latestParam)
            }
        }
    }
}
