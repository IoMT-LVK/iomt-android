/**
 * Settings view
 */

package com.iomt.android.jetpack.view.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.jetpack.theme.colorScheme

/**
 * @param signOut
 */
@Composable
fun SettingsView(signOut: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        DataTransferringSection()
        AccountSection(signOut)
    }
}

@Composable
private fun DataTransferringSection() = Section("Data transferring") {
    var isMobileNetworkEnabled by remember { mutableStateOf(false) }
    var isWifiPreferred by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Text("Use mobile network")
        Switch(isMobileNetworkEnabled, {
            isMobileNetworkEnabled = !isMobileNetworkEnabled
            if (!isMobileNetworkEnabled) {
                isWifiPreferred = true
            }
        })
    }
    Divider()
    Column(Modifier.padding(10.dp)) {
        Text("Main data transferring network")
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                RadioButton(isWifiPreferred, { isWifiPreferred = true })
                Text("Wifi")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                RadioButton(!isWifiPreferred, { isWifiPreferred = false }, enabled = isMobileNetworkEnabled)
                Text("Mobile")
            }
        }
    }
}

@Composable
private fun AccountSection(onAccountExit: () -> Unit) = Section("Account") {
    TextButton(
        onClick = onAccountExit,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            containerColor = MaterialTheme.colorScheme.background
        ),
    ) { Text("Exit account") }
}

@Composable
private fun Section(sectionName: String, content: @Composable () -> Unit) {
    Column {
        Row(Modifier.padding(5.dp)) { Text(sectionName) }
        OutlinedCard(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Column(Modifier.align(Alignment.CenterHorizontally)) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun SettingsViewPreview() {
    MaterialTheme(colorScheme) { SettingsView { } }
}
