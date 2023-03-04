package com.iomt.android.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name
 * @property surname
 * @property patronymic
 * @property birthdate
 * @property email
 * @property phoneNumber
 * @property login
 * @property password
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
)
