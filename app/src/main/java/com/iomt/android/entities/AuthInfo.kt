package com.iomt.android.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthInfo(
    val jwt: String,
    @SerialName("user_id")
    val userId: String,
    val confirmed: Boolean,
    val wasFailed: Boolean = false,
)
