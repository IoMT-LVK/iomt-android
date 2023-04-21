/**
 * File that defines current ColorScheme
 */

package com.iomt.android.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Suppress("CUSTOM_GETTERS_SETTERS")
val colorScheme: ColorScheme
    @Composable
    get() = getPreferredColorScheme()

/**
 * @return [ColorScheme] selected depending on current OS [ColorScheme]
 */
@Composable
fun getPreferredColorScheme(): ColorScheme {
    val isDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isDarkTheme = isSystemInDarkTheme()

    return when {
        isDynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
        isDynamicColor && !isDarkTheme -> dynamicLightColorScheme(LocalContext.current)
        isDarkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
}
