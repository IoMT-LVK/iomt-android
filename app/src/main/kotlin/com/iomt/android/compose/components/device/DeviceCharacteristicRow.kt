/**
 * Row for a characteristic of DeviceBodyCard
 */

package com.iomt.android.compose.components.device

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.compose.components.charts.LineChart

import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.entities.Characteristic

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * @param deviceMac MAC address of Bluetooth LE device
 * @param characteristic [Characteristic] that should be displayed in a row
 */
@Composable
internal fun DeviceCharacteristicRow(deviceMac: String, characteristic: Characteristic) {
    var value by remember { mutableStateOf("- -") }
    LaunchedEffect(characteristic.name) {
        characteristic.valueStateFlow
            .onEach { value = it }
            .onCompletion { possibleException ->
                possibleException?.let { exception ->
                    if (exception !is CancellationException) {
                        Log.e("DeviceCharacteristicRow", "Error occurred", exception)
                    }
                }
            }
            .launchIn(this)
    }

    var expanded by remember { mutableStateOf(false) }

    Card {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(painterResource(characteristic.getIcon()), characteristic.name, Modifier.height(24.dp).wrapContentHeight())
            Spacer(Modifier.padding(vertical = 25.dp, horizontal = 50.dp))
            Text(characteristic.prettyName)
            Spacer(Modifier.padding(vertical = 25.dp, horizontal = 50.dp))
            Text(value)
        }
        Divider()
        AnimatedVisibility(expanded) { LineChart(deviceMac, characteristic, expanded) }
    }
}

@Preview
@Composable
private fun DeviceCharacteristicRowPreview() {
    MaterialTheme(colorScheme) { DeviceCharacteristicRow("", Characteristic.stub) }
}
