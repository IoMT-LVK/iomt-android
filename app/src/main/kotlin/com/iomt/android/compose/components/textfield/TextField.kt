/**
 * Row with text field with icon
 */

package com.iomt.android.compose.components.textfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme

/**
 * @param cell [Cell] that should be displayed
 * @param verticalPadding vertical padding applied to [TextField]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(cell: Cell, verticalPadding: Dp = 5.dp) {
    TextField(
        value = cell.value,
        onValueChange = cell.onValueChange,
        modifier = Modifier.fillMaxWidth().padding(vertical = verticalPadding),
        label = { cell.description?.let { Text(it) } },
        isError = cell.validator?.invoke("")?.not() ?: false,
    )
}

@Preview
@Composable
private fun TextFieldPreview() {
    var value by remember { mutableStateOf("15.06.2001") }
    MaterialTheme(colorScheme) {
        TextField(Cell(value, R.drawable.cake) { value = it }, 0.dp)
    }
}
