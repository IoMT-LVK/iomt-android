/**
 * File containing Drawer header
 */

package com.iomt.android.compose.components.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme

/**
 * Drawer Header implementation
 */
@Composable
internal fun DrawerHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
        Image(painterResource(R.drawable.logo), "logo", Modifier.size(150.dp))
        Text(
            "IoMT Health",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            fontSize = TextUnit(5f, TextUnitType.Em),
        )
    }
}

@Preview
@Composable
private fun DrawerHeaderPreview() {
    MaterialTheme(colorScheme) { DrawerHeader() }
}
