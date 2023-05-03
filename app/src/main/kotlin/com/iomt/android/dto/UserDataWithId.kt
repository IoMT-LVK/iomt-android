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
 * @property allowed list of operator ids that have access to current user
 */
@Serializable
data class UserDataWithId(
    val id: Long,
    val login: String,
    val name: String,
    val surname: String,
    val patronymic: String? = null,
    val email: String,
    @SerialName("born") val birthdate: LocalDate? = null,
    val height: Int? = null,
    val weight: Int? = null,
    val allowed: List<Long>,
) {
    /**
     * @return [UserData] from [UserDataWithId]
     */
    fun toUserData() = UserData(login, name, surname, patronymic, email, birthdate, height, weight, allowed)

    companion object {
        val empty = UserDataWithId(-1, "", "", "", "", "", LocalDate(2001, 6, 15), null, null, emptyList())
    }
}
