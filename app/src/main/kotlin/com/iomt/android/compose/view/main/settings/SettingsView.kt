/**
 * Settings view
 */

package com.iomt.android.compose.view.main.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.iomt.android.compose.theme.colorScheme

/**
 * @param signOut callback to sign out
 */
@Composable
fun SettingsView(signOut: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        DataTransferringSection()
        DatabaseSection()
        AccountSection(signOut)
    }
}

@Preview
@Composable
private fun SettingsViewPreview() {
    MaterialTheme(colorScheme) { SettingsView { } }
}
