/**
 * File that contains EditableSection for AccountView
 */

@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.components.textfield.Cell
import com.iomt.android.compose.components.textfield.TextFieldWithIcon
import com.iomt.android.compose.theme.colorScheme

/**
 * @param title name of the section
 * @param fields [List] of [Cell]s that should be displayed in the section
 * @param onSave callback invoked on save button pressed
 */
@Composable
fun EditableSection(title: String, fields: List<Cell>, onSave: () -> Unit) {
    var isEdit by remember { mutableStateOf(false) }
    OutlinedCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.padding(10.dp))
                TextButton(onClick = {
                    if (isEdit) {
                        onSave()
                    }
                    isEdit = !isEdit
                }) {
                    Text(if (isEdit) "Save" else "Edit")
                }
            }
            fields.map { TextFieldWithIcon(it) }
        }
    }
}

@Preview
@Composable
private fun EditableSectionPreview() {
    var value by remember { mutableStateOf("15.06.2001") }
    MaterialTheme(colorScheme) {
        EditableSection("Section Name", listOf(Cell(value, R.drawable.cake) { value = it })) { }
    }
}
