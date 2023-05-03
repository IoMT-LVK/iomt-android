/**
 * File containing DatabaseSection of SettingsView
 */

package com.iomt.android.compose.view.main.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.iomt.android.room.record.RecordRepository

/**
 * Section corresponding to database statistics
 *
 * @return Unit
 */
@Composable
internal fun DatabaseSection() = Section("Database") {
    val recordRepository = RecordRepository(LocalContext.current)
    var totalAmount by remember { mutableStateOf(-1L) }
    var synchronizedAmount by remember { mutableStateOf(-1L) }

    LaunchedEffect(recordRepository) {
        totalAmount = recordRepository.countAll()
        synchronizedAmount = recordRepository.countSynchronized()
    }

    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Records in database")
            Text(totalAmount.toString())
        }
        Divider(Modifier.padding(vertical = 5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Synchronized records in database")
            Text(synchronizedAmount.toString())
        }
    }
}

@Preview
@Composable
private fun DatabaseSectionPreview() {
    DatabaseSection()
}
