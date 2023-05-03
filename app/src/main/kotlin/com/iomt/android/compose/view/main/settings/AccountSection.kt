/**
 * File containing AccountSection of SettingsView
 */

package com.iomt.android.compose.view.main.settings

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.iomt.android.compose.theme.colorScheme

/**
 * Section corresponding to account settings
 *
 * @param onAccountExit callback invoked on `Exit account` button pressed
 * @return Unit
 */
@Composable
internal fun AccountSection(onAccountExit: () -> Unit) = Section("Account") {
    TextButton(
        onClick = onAccountExit,
        colors = ButtonDefaults.textButtonColors(
            contentColor = colorScheme.error,
            containerColor = colorScheme.background,
        ),
    ) { Text("Exit account") }
}

@Preview
@Composable
private fun AccountSectionPreview() {
    AccountSection { }
}
