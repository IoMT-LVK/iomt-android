/**
 * File containing date picker for birthdate setting
 */

package com.iomt.android.compose.components

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iomt.android.R
import com.iomt.android.compose.theme.colorScheme
import java.util.*

/**
 * @param date [MutableState] of current birthdate as [String]
 * @param isDateValid flag that defines if [date] is considered to be valid or not
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerTextField(date: MutableState<String>, isDateValid: Boolean) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val calendarYear = calendar.get(Calendar.YEAR)
    val calendarMonth = calendar.get(Calendar.MONTH)
    val calendarDay = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker,
            year: Int,
            month: Int,
            dayOfMonth: Int ->
            date.value = "${"%02d".format(dayOfMonth)}.${"%02d".format(month + 1)}.${"%04d".format(year)}"
        }, calendarYear, calendarMonth, calendarDay,
    )

    Column(
        modifier = Modifier.fillMaxSize().clickable { datePickerDialog.show() },
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(
            date.value,
            onValueChange = { },
            isError = isDateValid,
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
            enabled = false,
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.cake),
                    "birthdate",
                    Modifier.padding(10.dp).size(24.dp),
                    tint = colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}
