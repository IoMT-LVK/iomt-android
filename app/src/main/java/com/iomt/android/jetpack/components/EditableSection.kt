@file:Suppress("FILE_NAME_MATCH_CLASS")

package com.iomt.android.jetpack.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * @property iconPainter
 * @property value
 * @property validator
 * @property onValueChange
 */
data class Cell(
    val iconPainter: Painter,
    val value: String,
    val validator: ((String) -> Boolean)? = null,
    val onValueChange: (String) -> Unit,
)

/**
 * @param title
 * @param fields
 * @param onSave
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
