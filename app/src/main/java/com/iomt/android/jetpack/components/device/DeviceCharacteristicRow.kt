/**
 * Row for a characteristic of DeviceBodyCard
 */

package com.iomt.android.jetpack.components.device

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.entities.Characteristic
import com.iomt.android.jetpack.theme.colorScheme

/**
 * @param characteristic [Characteristic] that should be displayed in a row
 */
@Composable
internal fun DeviceCharacteristicRow(characteristic: Characteristic) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(characteristic.getIcon()), characteristic.name, Modifier.height(24.dp).wrapContentHeight())
        Spacer(Modifier.padding(vertical = 25.dp, horizontal = 50.dp))
        Text(characteristic.prettyName)
        Spacer(Modifier.padding(vertical = 25.dp, horizontal = 50.dp))
        Text(characteristic.value)
        Log.e("DeviceCharacteristicRow", characteristic.value)
    }
}

@Preview
@Composable
private fun DeviceCharacteristicRowPreview() {
    MaterialTheme(colorScheme) { DeviceCharacteristicRow(Characteristic.stub) }
}
