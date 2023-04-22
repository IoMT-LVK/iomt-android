/**
 * Row with text field with icon
 */

package com.iomt.android.compose.components.textfield

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme

/**
 * @param cell [Cell] that should be displayed
 * @throws IllegalStateException when no icon was passed in [Cell]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithIcon(cell: Cell, enabled: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        cell.iconId?.let { id ->
            Icon(
                painterResource(id),
                cell.description,
                Modifier.padding(10.dp).size(24.dp),
                tint = colorScheme.onSurfaceVariant,
            )
        } ?: throw IllegalStateException("Could not display null icon")
        TextField(cell.value, onValueChange = cell.onValueChange, Modifier.weight(1f), enabled = enabled)
    }
}

@Preview
@Composable
private fun TextFieldWithIconPreview() {
    var value by remember { mutableStateOf("15.06.2001") }
    MaterialTheme(colorScheme) {
        TextFieldWithIcon(Cell(value, R.drawable.cake) { value = it },true)
    }
}
