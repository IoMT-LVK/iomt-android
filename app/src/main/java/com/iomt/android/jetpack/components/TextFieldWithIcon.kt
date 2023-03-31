/**
 * Row with text field with icon
 */

package com.iomt.android.jetpack.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @param cell
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithIcon(cell: Cell) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(cell.iconPainter, cell.description, Modifier.padding(10.dp).size(24.dp))
        TextField(cell.value, onValueChange = cell.onValueChange, Modifier.weight(1f))
    }
}
