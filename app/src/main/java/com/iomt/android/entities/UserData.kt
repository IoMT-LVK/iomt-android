package com.iomt.android.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name
 * @property surname
 * @property patronymic
 * @property email
 * @property birthdate
 * @property phoneNumber
 * @property height
 * @property weight
 * @property jwt
 * @property userId
 */
@Serializable
data class UserData(
    @SerialName("sname") val name: String,
    @SerialName("ssurname") val surname: String,
    val patronymic: String?,
    val email: String?,
    val birthdate: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    val height: Int,
    val weight: Int,
    val jwt: String,
    val userId: String,
)
