package com.iomt.android.entities

import android.util.Patterns
import com.iomt.android.utils.isLetters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name name of a user
 * @property surname surname of a user
 * @property patronymic patronymic of a user
 * @property birthdate birthdate of a user in format dd.mm.yyyy
 * @property email email of a user
 * @property phoneNumber phone number with leading plus (e.g. +78005553535)
 * @property login login of a user
 * @property password password of a user
 */
@Serializable
data class SignUpInfo(
    val name: String,
    val surname: String,
    val patronymic: String,
    val birthdate: String,
    val email: String,
    @SerialName("phone_number") val phoneNumber: String,
    val login: String,
    val password: String,
) {
    /**
     * @return true if [name] is valid, false otherwise
     */
    fun isNameValid() = name.isNotBlank() && name.isLetters()

    /**
     * @return true if [surname] is valid, false otherwise
     */
    fun isSurnameValid() = surname.isNotBlank() && surname.isLetters()

    /**
     * @return true if [patronymic] is valid, false otherwise
     */
    fun isPatronymicValid() = patronymic.isNotBlank() && patronymic.isLetters()

    /**
     * @return true if [birthdate] is valid, false otherwise
     */
    fun isBirthdateValid() = birthdate.isNotBlank() && birthdate.matches(dateRegex)

    /**
     * @return true if [email] is valid, false otherwise
     */
    fun isEmailValid() = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    /**
     * @return true if [phoneNumber] is valid, false otherwise
     */
    fun isPhoneValid() = phoneNumber.isNotBlank() && Patterns.PHONE.matcher(phoneNumber).matches()

    /**
     * @return true if [login] is valid, false otherwise
     */
    fun isLoginValid() = login.isNotBlank() && login.length > 3

    /**
     * @return true if [password] is valid, false otherwise
     */
    fun isPasswordValid() = isPasswordValid(password)

    private fun isNameSectionValid() = isNameValid() && isSurnameValid() && isPatronymicValid()
    private fun isParamSectionValid() = isBirthdateValid() && isEmailValid() && isPhoneValid()
    private fun isLoginSectionValid() = isLoginValid() && isPasswordValid()

    /**
     * @return true if [SignUpInfo] is valid, false otherwise
     */
    fun isValid() = isNameSectionValid() && isParamSectionValid() && isLoginSectionValid()

    companion object {
        /**
         * @param password user password
         * @return true if [password] is valid, false otherwise
         */
        fun isPasswordValid(password: String) = password.length in 4..15

        private val dateRegex = "^(3[01]|[12][0-9]|0[1-9])\\.(1[0-2]|0[1-9])\\.[0-9]{4}\$".toRegex()

        /**
         * Empty [SignUpInfo]
         */
        val empty = SignUpInfo(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
        )
    }
}
