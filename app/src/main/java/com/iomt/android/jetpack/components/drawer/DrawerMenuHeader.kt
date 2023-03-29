/**
 * Drawer row with navigation
 */

package com.iomt.android.jetpack.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

/**
 * @param iconDrawableId
 * @param text
 * @param isSelected
 * @param onItemClick
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
                    background(MaterialTheme.colorScheme.primaryContainer)
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
