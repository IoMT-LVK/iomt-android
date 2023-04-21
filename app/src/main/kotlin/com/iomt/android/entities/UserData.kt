package com.iomt.android.entities

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name
 * @property surname
 * @property patronymic
 * @property email
 * @property birthdate
 * @property height
 * @property weight
 * @property id
 * @property login
 * @property allowed
 */
@Serializable
data class UserData(
    val id: Long,
    val login: String,
    val name: String,
    val surname: String,
    val patronymic: String?,
    val email: String,
    @SerialName("born") val birthdate: LocalDate?,
    val height: Int?,
    val weight: Int?,
    val allowed: List<Long>,
)
