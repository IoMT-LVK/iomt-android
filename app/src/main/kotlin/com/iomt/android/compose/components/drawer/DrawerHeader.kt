/**
 * File containing Drawer header
 */

package com.iomt.android.compose.components.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme
import com.iomt.android.dto.UserDataWithId
import com.iomt.android.http.RequestParams

/**
 * Drawer Header implementation
 */
@Composable
internal fun DrawerHeader() {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Image(painterResource(R.drawable.logo), "logo", Modifier.size(150.dp))
        Column {
            Text(
                "IoMT Health",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                fontSize = TextUnit(5f, TextUnitType.Em),
            )
            RequestParams.userData
                ?.run { "$name ${patronymic?.take(1)} $surname" to login }
                ?.let { (name, login) ->
                    Spacer(Modifier.padding(10.dp))
                    Text(
                        name,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = TextUnit(4f, TextUnitType.Em),
                    )
                    Spacer(Modifier.padding(3.dp))
                    Text(
                        login,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = TextUnit(3f, TextUnitType.Em),
                    )
                }
        }
    }
}

@Preview
@Composable
private fun DrawerHeaderPreview() {
    RequestParams.userData = UserDataWithId.empty.copy(login = "sanyavertolet", name = "Sanya", surname = "Vertolet")
    MaterialTheme(colorScheme) { DrawerHeader() }
}
