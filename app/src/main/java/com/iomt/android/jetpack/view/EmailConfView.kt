/**
 * Email confirmation view
 */

package com.iomt.android.jetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.iomt.android.R

/**
 * @param preNavController
 */
@Composable
fun EmailConfView(preNavController: NavHostController) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.padding(50.dp))
        Icon(painterResource(R.drawable.check), null, tint = Color.Green, modifier = Modifier.size(200.dp))
        Spacer(Modifier.padding(25.dp))
        Text("Sent email confirmation", fontSize = TextUnit(8f, TextUnitType.Em))
        Spacer(Modifier.padding(100.dp))
        OutlinedButton({ preNavController.navigate("login") }) {
            Text("Back", fontSize = TextUnit(5f, TextUnitType.Em), color = Color.Black)
        }
    }
}
