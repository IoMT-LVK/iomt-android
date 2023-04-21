package com.iomt.android.compose.components.textfield

import com.iomt.android.R

/**
 * @property value current cell value
 * @property iconId id of an icon that should be displayed in [Cell] row, null by default
 * @property description label that should be displayed
 * @property validator callback that validates the value
 * @property onValueChange callback invoked on [value] change
 */
data class Cell(
    val value: String,
    val iconId: Int? = null,
    val description: String? = null,
    val validator: ((String) -> Boolean)? = null,
    val onValueChange: (String) -> Unit,
)

/**
 * @param value current cell value
 * @param validator callback that validates the value
 * @param onValueChange callback invoked on [value] change
 * @return [Cell] for weight
 */
fun weightCell(
    value: String,
    validator: ((String) -> Boolean)? = null,
    onValueChange: (String) -> Unit,
) = Cell(value, R.drawable.weight_scale, "weight", validator, onValueChange)

/**
 * @param value current cell value
 * @param validator callback that validates the value
 * @param onValueChange callback invoked on [value] change
 * @return [Cell] for height
 */
fun heightCell(
    value: String,
    validator: ((String) -> Boolean)? = null,
    onValueChange: (String) -> Unit,
) = Cell(value, R.drawable.height, "height", validator, onValueChange)

/**
 * @param value current cell value
 * @param validator callback that validates the value
 * @param onValueChange callback invoked on [value] change
 * @return [Cell] for birthdate
 */
fun birthdateCell(
    value: String,
    validator: ((String) -> Boolean)? = null,
    onValueChange: (String) -> Unit,
) = Cell(value, R.drawable.cake, "birthdate", validator, onValueChange)

/**
 * @param value current cell value
 * @param validator callback that validates the value
 * @param onValueChange callback invoked on [value] change
 * @return [Cell] for phone number
 */
fun phoneCell(
    value: String,
    validator: ((String) -> Boolean)? = null,
    onValueChange: (String) -> Unit,
) = Cell(value, R.drawable.call, "phone", validator, onValueChange)

/**
 * @param value current cell value
 * @param validator callback that validates the value
 * @param onValueChange callback invoked on [value] change
 * @return [Cell] for email
 */
fun emailCell(
    value: String,
    validator: ((String) -> Boolean)? = null,
    onValueChange: (String) -> Unit,
) = Cell(value, R.drawable.mail, "email", validator, onValueChange)
