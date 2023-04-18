/**
 * Settings view
 */

package com.iomt.android.jetpack.view.main

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.iomt.android.R
import com.iomt.android.jetpack.theme.colorScheme
import com.iomt.android.utils.SharedPreferencesNames

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/**
 * @param signOut callback to sign out
 */
@Composable
fun SettingsView(signOut: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        DataTransferringSection()
        AccountSection(signOut)
    }
}

private fun getMqttWorkPeriodRange(begin: Duration, end: Duration) = begin
    .toLong(DurationUnit.MILLISECONDS)
    .toFloat()..end
    .toLong(DurationUnit.MILLISECONDS)
    .toFloat()

private fun getMqttWorkPeriodSteps(
    begin: Duration,
    end: Duration,
    stepSize: Duration = begin,
) = (end - begin).toLong(DurationUnit.MINUTES).div(stepSize.toLong(DurationUnit.MINUTES)).toInt() - 1

@Composable
private fun DataTransferringSection() = Section("Data transferring") {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences(
        stringResource(R.string.ACC_DATA),
        Context.MODE_PRIVATE,
    )
    var isMobileNetworkEnabled by remember { mutableStateOf(false) }
    var isWifiPreferred by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Use mobile network")
        Switch(
            isMobileNetworkEnabled,
            {
                isMobileNetworkEnabled = !isMobileNetworkEnabled
                sharedPreferences
                    .edit()
                    .apply { putBoolean(SharedPreferencesNames.mobileNetwork, true) }
                    .apply()
                if (!isMobileNetworkEnabled) {
                    isWifiPreferred = true
                }
            },
            Modifier.padding(horizontal = 10.dp)
        )
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
    Divider()
    var mqttWorkerPeriod by remember {
        mutableStateOf(
            sharedPreferences.getLong(
                SharedPreferencesNames.mqttWorkerPeriod,
                5.minutes.toLong(DurationUnit.MILLISECONDS),
            ).toFloat()
        )
    }
    Column(Modifier.padding(10.dp)) {
        Text("Synchronization period")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(
                value = mqttWorkerPeriod,
                onValueChange = { mqttWorkerPeriod = it },
                onValueChangeFinished = {
                    sharedPreferences.edit()
                        .apply { putLong(SharedPreferencesNames.mqttWorkerPeriod, mqttWorkerPeriod.toLong()) }
                        .apply()
                },
                valueRange = getMqttWorkPeriodRange(1.minutes, 30.minutes),
                steps = getMqttWorkPeriodSteps(1.minutes, 30.minutes),
            )
            val duration = mqttWorkerPeriod.toLong().milliseconds.toString()
            Text(duration)
        }
    }
}

@Composable
private fun AccountSection(onAccountExit: () -> Unit) = Section("Account") {
    TextButton(
        onClick = onAccountExit,
        colors = ButtonDefaults.textButtonColors(
            contentColor = colorScheme.error,
            containerColor = colorScheme.background
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
