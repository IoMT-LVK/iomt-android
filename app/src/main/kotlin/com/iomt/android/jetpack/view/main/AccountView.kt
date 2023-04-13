@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.jetpack.view.main

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.bluetooth.BleForegroundService
import com.iomt.android.jetpack.components.*
import com.iomt.android.jetpack.components.textfield.*
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.utils.getService

/**
 * @property prettyName human-readable tab name
 * @property tabIndex tab index
 */
@Suppress("WRONG_DECLARATIONS_ORDER")
private enum class AccountViewTabs(val prettyName: String, val tabIndex: Int) {
    USER("User info", 0),
    DEVICES("Devices", 1),
    ;
    companion object {
        val default = USER
    }
}

/**
 * View for account
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun AccountView() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        var selectedTab by remember { mutableStateOf(AccountViewTabs.default) }
        val avatarPainter = painterResource(id = R.drawable.logo)
        Column(Modifier.padding(20.dp)) {
            OutlinedCard(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Image(
                    painter = avatarPainter,
                    contentDescription = "Avatar",
                    Modifier.scale(0.85f).clickable { /* TODO: change avatar */ })

                TabRow(selectedTab.tabIndex) {
                    AccountViewTabs.values().map { tabs ->
                        Tab(
                            selected = tabs.tabIndex == selectedTab.tabIndex,
                            onClick = { selectedTab = tabs },
                            text = { Text(tabs.prettyName, color = colorScheme.primaryContainer) },
                        )
                    }
                }
            }
            when (selectedTab) {
                AccountViewTabs.USER -> RenderUserInfo()
                AccountViewTabs.DEVICES -> RenderConnectedDevices()
            }
        }
    }
}

@Composable
private fun RenderUserInfo() {
    var weight by remember { mutableStateOf(0.0) }
    var height by remember { mutableStateOf(0.0) }
    var birthdate by remember { mutableStateOf("dd.mm.yyyy") }
    var email by remember { mutableStateOf("example@iomt.com") }
    var phoneNumber by remember { mutableStateOf("+78005553535") }

    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceAround) {
        EditableSection("Personal data", listOf(
            weightCell(weight.toString()) { weight = it.toDouble() },
            heightCell(height.toString()) { height = it.toDouble() },
            birthdateCell(birthdate) { birthdate = it },
        )) {
            // send personal data update request
        }
        EditableSection("Contact Data", listOf(
            phoneCell(phoneNumber) { phoneNumber = it },
            emailCell(email) { email = it },
        )) {
            // send contact data update request
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
private fun RenderConnectedDevices() {
    val bleForegroundService: BleForegroundService = LocalContext.getService()
    val connectedDevices = remember { bleForegroundService.getConnectedDevices().toMutableStateList() }
    DeviceList("Connected devices", connectedDevices) { }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Preview
@Composable
private fun AccountViewPreview() {
    MaterialTheme(colorScheme) { AccountView() }
}
