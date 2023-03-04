/**
 * File that is responsible for input form validation
 */

package com.iomt.android

import android.util.Patterns
import android.widget.EditText
import java.util.*

private const val BIRTHDAY_ERROR_TEXT = "Введите корректно дату рождения"
private const val EMAIL_ERROR_TEXT = "Введите корректно e-mail"
private const val LOGIN_ERROR_TEXT = "Не менее 2 символов"
private const val NAME_ERROR_TEXT = "Не менее 2 символов"
private const val NO_BIRTHDAY_ERROR_TEXT = "Введите дату рождения"
private const val PASSWORD_ERROR_TEXT = "Не менее 4 и не более 14 символов"
private const val PATRONYMIC_ERROR_TEXT = "Не менее 2 символов"
private const val PHONE_NUMBER_ERROR_TEXT = "Введите корректно номер телефона"
private const val SECOND_PASSWORD_ERROR_TEXT = "Пароли не совпадают"
private const val SURNAME_ERROR_TEXT = "Не менее 2 символов"
private const val PHONE_NUMBER_LENGTH = 11
private const val MIN_PASSWORD_LENGTH = 4
private const val MAX_PASSWORD_LENGTH = 14

/**
 * @return true if name is valid, false otherwise
 */
fun EditText.isNameValid() = validateStringForm(this, NAME_ERROR_TEXT)

/**
 * @return true if surname is valid, false otherwise
 */
fun EditText.isSurnameValid() = validateStringForm(this, SURNAME_ERROR_TEXT)

/**
 * @return true if patronymic is valid, false otherwise
 */
fun EditText.isPatronymicValid() = validateStringForm(this, PATRONYMIC_ERROR_TEXT)

/**
 * @return true if birthdate is valid, false otherwise
 */
fun EditText.isBirthdateValid() = validateStringForm(this, NO_BIRTHDAY_ERROR_TEXT) { it.isEmpty() }

/**
 * @param year
 * @param month
 * @param day
 * @return true if birthday is valid, false otherwise
 */
fun EditText.isBirthdayValid(year: Int, month: Int, day: Int): Boolean {
    val calendar = Calendar.getInstance()
    val calYear = calendar[Calendar.YEAR]
    val calMonth = calendar[Calendar.MONTH]
    val calDay = calendar[Calendar.DAY_OF_MONTH]
    return if (
        year == 0 ||
                year > calYear ||
                year == calYear && month > calMonth ||
                year == calYear && month == calMonth && day > calDay
    ) {
        error = BIRTHDAY_ERROR_TEXT
        false
    } else {
        error = null
        true
    }
}

/**
 * @return true if login is valid, false otherwise
 */
fun EditText.isLoginValid() = validateStringForm(this, LOGIN_ERROR_TEXT)

/**
 * @return true if email is valid, false otherwise
 */
fun EditText.isEmailValid() = validateStringForm(this, EMAIL_ERROR_TEXT) {
    it.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(it).matches()
}

/**
 * @return true if name is phone number, false otherwise
 */
fun EditText.isPhoneNumberValid() = validateStringForm(this, PHONE_NUMBER_ERROR_TEXT) {
    it.length != PHONE_NUMBER_LENGTH
}

/**
 * @return true if first password is valid, false otherwise
 */
fun EditText.isFirstPasswordValid(): Boolean = validateStringForm(this, PASSWORD_ERROR_TEXT) {
    it.length < MIN_PASSWORD_LENGTH || it.length > MAX_PASSWORD_LENGTH
}

/**
 * @return true if second password is valid, false otherwise
 */
fun EditText.isSecondPasswordValid(): Boolean = validateStringForm(this, SECOND_PASSWORD_ERROR_TEXT) {
    it.length < MIN_PASSWORD_LENGTH || it.length > MAX_PASSWORD_LENGTH
}

private fun validateStringForm(
    form: EditText,
    errorText: String,
    isInvalidFunc: (String) -> Boolean = { it.length < 2 },
) = form.text.toString().let { textFromForm ->
    if (isInvalidFunc(textFromForm)) {
        form.error = errorText
        false
    } else {
        form.error = null
        true
    }
}
