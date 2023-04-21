package com.iomt.android.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property id user id
 * @property login user login
 * @property name user name
 * @property surname user surname
 * @property patronymic user patronymic
 * @property email user email
 * @property birthdate user birthdate as [Instant]
 * @property height user height
 * @property weight user weight
 * @property allowed
 */
@Serializable
data class UserDataWithId(
    val id: Long,
    val login: String,
    val name: String,
    val surname: String,
    val patronymic: String?,
    val email: String,
    @SerialName("born") val birthdate: Instant?,
    val height: Double?,
    val weight: Double?,
    val allowed: List<Long>,
) {
    fun toUserData() = UserData(login, name, surname, patronymic, email, birthdate, height, weight, allowed)
}
