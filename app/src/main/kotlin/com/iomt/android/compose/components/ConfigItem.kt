/**
 * File containing component for DeviceConfig rendering
 */

package com.iomt.android.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.configs.DeviceConfig

/**
 * Pretty [DeviceConfig] card
 *
 * @param config [DeviceConfig] to be rendered
 * @param onClick callback invoked when [ConfigItem] is clicked
 */
@Composable
fun ConfigItem(config: DeviceConfig, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        with(config.general) {
            Text(name, modifier = Modifier.padding(start = 20.dp), fontSize = TextUnit(4f, TextUnitType.Em))
            Icon(
                painterResource(type.iconId),
                type.name.lowercase(),
                Modifier.size(48.dp).padding(end = 20.dp),
            )
        }
    }
}

@Preview
@Composable
private fun ConfigItemPreview() {
    ConfigItem(DeviceConfig.stub) { }
}
