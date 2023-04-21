/**
 * File containing component for BluetoothDevice rendering
 */

package com.iomt.android.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

/**
 * Pretty device card
 *
 * @param name device name
 * @param macAddress MAC address of device
 * @param onClick callback invoked when [DeviceItem] is clicked
 */
@Composable
fun DeviceItem(name: String, macAddress: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(name, Modifier.padding(start = 10.dp), fontSize = TextUnit(4f, TextUnitType.Em))
        Text(macAddress, fontSize = TextUnit(3f, TextUnitType.Em))
    }
}

@Preview
@Composable
private fun DeviceItemPreview() {
    DeviceItem("Mi Band 6", "00-B0-D0-63-C2-26") { }
}
