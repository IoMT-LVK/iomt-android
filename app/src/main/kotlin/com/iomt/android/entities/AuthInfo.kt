package com.iomt.android.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property jwt jwt-token received from backend
 * @property userId id of current user
 * @property confirmed is confirmed
 * @property wasFailed was error found
 */
@Serializable
data class AuthInfo(
    val jwt: String,
    @SerialName("user_id")
    val userId: String,
    val confirmed: Boolean,
    val wasFailed: Boolean = false,
) {
    companion object {
        val empty = AuthInfo("", "", false, wasFailed = true)
    }
}
