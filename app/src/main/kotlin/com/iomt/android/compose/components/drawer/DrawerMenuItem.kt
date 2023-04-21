/**
 * Drawer row with navigation
 */

package com.iomt.android.compose.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme

/**
 * @param iconDrawableId icon that should be displayed for [Drawer] menu item
 * @param text text that should be displayed for [Drawer] menu item
 * @param isSelected flag that defines if this item is currently selected
 * @param onItemClick callback invoked on item click
 */
@Composable
internal fun DrawerMenuItem(
    iconDrawableId: Int,
    text: String,
    isSelected: Boolean,
    onItemClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .size(75.dp)
            .clickable(true, onClick = onItemClick)
            .apply {
                if (isSelected) {
                    background(colorScheme.primaryContainer)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconDrawableId),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = TextUnit(6f, TextUnitType.Em))
    }
}

@Preview
@Composable
private fun DrawerMenuItemPreview() {
    var isSelected by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme) {
        DrawerMenuItem(R.drawable.ic_menu_home, "Home", false) { isSelected = !isSelected }
    }
}
