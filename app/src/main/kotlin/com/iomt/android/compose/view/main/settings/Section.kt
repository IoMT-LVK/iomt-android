/**
 * File containing base section for SettingsView
 */

package com.iomt.android.compose.view.main.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @param sectionName name of a section that should be displayed
 * @param content section content
 */
@Composable
internal fun Section(sectionName: String, content: @Composable () -> Unit) {
    Column {
        Row(Modifier.padding(5.dp)) { Text(sectionName) }
        OutlinedCard(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Column(Modifier.align(Alignment.CenterHorizontally)) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun SectionPreview() {
    Section("Preview") { Text("This is just a preview") }
}
