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
 */
@Serializable
data class UserData(
    val name: String,
    val surname: String,
    val patronymic: String?,
    val email: String?,
    val birthdate: String?,
    @SerialName("phone_number") val phoneNumber: String?,
    val height: Int,
    val weight: Int,
)
