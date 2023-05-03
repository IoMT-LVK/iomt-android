package com.iomt.android.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property login user login
 * @property name user name
 * @property surname user surname
 * @property patronymic user patronymic
 * @property email user email
 * @property birthdate user birthdate as [Instant]
 * @property height user height
 * @property weight user weight
 * @property allowed list of operator ids that have access to current user
 */
@Serializable
data class UserData(
    val login: String,
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    @SerialName("born") val birthdate: LocalDate? = null,
    val height: Int?,
    val weight: Int?,
    val allowed: List<Long>,
) {
    /**
     * @param id user id received from backend
     * @return [UserDataWithId] from [UserData]
     */
    fun toUserDataWithId(id: Long) = UserDataWithId(id, login, name, surname, patronymic, email, birthdate, height, weight, allowed)
}
