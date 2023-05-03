/**
 * File that contains EditableSection for AccountView
 */

@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.components.textfield.Cell
import com.iomt.android.compose.theme.colorScheme

/**
 * @param title name of the section
 * @param fields [List] of [Cell]s that should be displayed in the section
 * @param additionalContent additional content that is
 * @param onSave callback invoked on save button pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableSection(
    title: String,
    fields: List<Cell>,
    additionalContent: @Composable () -> Unit = { },
    onSave: () -> Unit,
) {
    OutlinedCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.padding(10.dp))
                TextButton(onClick = { onSave() }) { Text("Save") }
            }
            fields.map { cell ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = cell.value,
                        onValueChange = cell.onValueChange,
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            cell.iconId?.let { id ->
                                Icon(
                                    painterResource(id),
                                    cell.description,
                                    Modifier.padding(10.dp).size(24.dp),
                                    tint = colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                }
            }
            additionalContent()
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
