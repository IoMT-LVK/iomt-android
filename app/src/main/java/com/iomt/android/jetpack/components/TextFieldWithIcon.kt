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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.jetpack.theme.colorScheme

/**
 * @param cell [Cell] that should be displayed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithIcon(cell: Cell) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(cell.iconPainter, cell.description, Modifier.padding(10.dp).size(24.dp))
        TextField(cell.value, onValueChange = cell.onValueChange, Modifier.weight(1f))
    }
}

@Preview
@Composable
private fun TextFieldWithIconPreview() {
    var value by remember { mutableStateOf("15.06.2001") }
    MaterialTheme(colorScheme) {
        TextFieldWithIcon(Cell(painterResource(R.drawable.cake), value) { value = it })
    }
}
