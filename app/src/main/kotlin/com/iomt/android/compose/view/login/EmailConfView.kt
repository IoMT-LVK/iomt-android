/**
 * Email confirmation view
 */

package com.iomt.android.compose.view.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme

/**
 * @param navigateToLogin callback to go back to [LoginView]
 */
@Composable
fun EmailConfView(navigateToLogin: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.padding(50.dp))
        Icon(painterResource(R.drawable.check), null, tint = Color.Green, modifier = Modifier.size(200.dp))
        Spacer(Modifier.padding(25.dp))
        Text("Sent email confirmation", fontSize = TextUnit(8f, TextUnitType.Em))
        Spacer(Modifier.padding(100.dp))
        OutlinedButton(navigateToLogin) {
            Text("Back", fontSize = TextUnit(5f, TextUnitType.Em), color = Color.Black)
        }
    }
}

@Preview
@Composable
private fun EmailConfViewPreview() {
    MaterialTheme(colorScheme) { EmailConfView { } }
}
