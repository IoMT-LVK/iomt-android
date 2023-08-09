@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.compose.view.main

import android.Manifest
import android.os.Build
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.iomt.android.bluetooth.BluetoothDeviceWithConfig
import com.iomt.android.compose.components.*
import com.iomt.android.compose.components.textfield.*
import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.http.RequestParams
import com.iomt.android.http.sendUserData
import com.iomt.android.utils.isBirthdateValidFun
import com.iomt.android.utils.parseFromDotFormat
import com.iomt.android.utils.rememberBoundService
import com.iomt.android.utils.withLoading

import io.ktor.http.*

import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * @property prettyName human-readable tab name
 */
@Suppress("WRONG_DECLARATIONS_ORDER")
private enum class AccountViewTabs(val prettyName: String) {
    USER("User info"),
    DEVICES("Devices"),
    ;
    companion object {
        val default = USER
    }
}

/**
 * View for account
 *
 * @param onDeviceItemClick callback invoked on device item click
 */
@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun AccountView(onDeviceItemClick: (BluetoothDeviceWithConfig) -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        var selectedTab by remember { mutableStateOf(AccountViewTabs.default) }
        val avatarPainter = painterResource(id = R.drawable.logo)
        Column(Modifier.padding(20.dp)) {
            OutlinedCard(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Image(
                    painter = avatarPainter,
                    contentDescription = "Avatar",
                    Modifier.scale(0.85f).clickable { /* TODO: change avatar */ },
                )
                TabRow(selectedTab.ordinal) {
                    AccountViewTabs.values().map { tabs ->
                        Tab(
                            selected = tabs.ordinal == selectedTab.ordinal,
                            onClick = { selectedTab = tabs },
                            text = { Text(tabs.prettyName, color = TabRowDefaults.contentColor) },
                        )
                    }
                }
            }
            when (selectedTab) {
                AccountViewTabs.USER -> RenderUserInfo()
                AccountViewTabs.DEVICES -> RenderConnectedDevices(onDeviceItemClick)
            }
        }
    }
}

@Composable
private fun RenderUserInfo() {
    var currentUserData by remember {
        mutableStateOf(requireNotNull(RequestParams.userData) { "No user data is fetched" }.toUserData())
    }
    var weight by remember { mutableStateOf(currentUserData.weight?.toString().orEmpty()) }
    var isWeightValid by remember { mutableStateOf(true) }
    var height by remember { mutableStateOf(currentUserData.height?.toString().orEmpty()) }
    var isHeightValid by remember { mutableStateOf(true) }
    val birthdate = remember { mutableStateOf("dd.mm.yyyy") }
    var isBirthdateValid by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf(currentUserData.email) }
    var isEmailValid by remember { mutableStateOf(true) }
    val getUpdatedPersonalData = {
        val newWeight = try {
            weight.toInt().also { isWeightValid = true }
        } catch (exception: NumberFormatException) {
            currentUserData.weight.also { isWeightValid = false }
        }

        val newHeight = try {
            height.toInt().also { isHeightValid = true }
        } catch (exception: NumberFormatException) {
            currentUserData.height.also { isHeightValid = false }
        }

        val newBirthdate = try {
            if (isBirthdateValidFun(birthdate.value)) {
                LocalDate.parseFromDotFormat(birthdate.value).also { Log.d("", it.toString()) }
            } else {
                isBirthdateValid = false
                currentUserData.birthdate
            }
        } catch (exception: NumberFormatException) {
            currentUserData.birthdate.also { isBirthdateValid = false }
        }

        val newEmail = if (email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email
        } else {
            currentUserData.email.also { isEmailValid = false }
        }

        currentUserData.copy(
            weight = newWeight,
            height = newHeight,
            birthdate = newBirthdate,
            email = newEmail,
        )
    }

    val scope = rememberCoroutineScope()
    val errorToast = Toast.makeText(LocalContext.current, "Failed to update data", Toast.LENGTH_SHORT)
    Column(
        modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        EditableSection(
            "Account data",
            listOf(
                emailCell(email, { isEmailValid }) { email = it },
                weightCell(weight, { isWeightValid }) { weight = it },
                heightCell(height, { isHeightValid }) { height = it },
            ),
            additionalContent = { DatePickerTextField(birthdate, isBirthdateValid) },
        ) {
            val updatedUserData = getUpdatedPersonalData()
            if (isWeightValid || isHeightValid || isEmailValid || isBirthdateValid) {
                scope.launch {
                    if (sendUserData(updatedUserData).isSuccess()) {
                        RequestParams.userData = currentUserData.toUserDataWithId(RequestParams.userData?.id!!)
                    } else {
                        errorToast.show()
                        currentUserData = RequestParams.userData?.toUserData()!!
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
private fun RenderConnectedDevices(onItemClick: (BluetoothDeviceWithConfig) -> Unit) {
    val bluetoothLeForegroundService by rememberBoundService().collectAsState()
    withLoading(bluetoothLeForegroundService) { foregroundService ->
        val connectedDevices = remember {
            foregroundService.getConnectedDevicesWithConfigs().toMutableStateList()
        }
        DeviceList("Connected devices", connectedDevices, onItemClick)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Preview
@Composable
private fun AccountViewPreview() {
    MaterialTheme(colorScheme) { AccountView { } }
}
