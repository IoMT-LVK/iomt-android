/**
 * Device body card of DeviceView
 */

package com.iomt.android.compose.components.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.entities.Characteristic

/**
 * @param characteristics list of [Characteristic]s that should be displayed in body card of device
 */
@Composable
fun DeviceBodyCard(characteristics: List<Characteristic>) {
    OutlinedCard(Modifier.fillMaxWidth()) {
        characteristics.toList()
            .sortedBy { characteristic -> characteristic.name }
            .map { characteristic -> DeviceCharacteristicRow(characteristic) }
    }
}

@Preview
@Composable
private fun DeviceBodyCardPreview() {
    val characteristics by remember { mutableStateOf(listOf(Characteristic.stub)) }
    MaterialTheme(colorScheme) { DeviceBodyCard(characteristics) }
}
