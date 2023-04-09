package com.iomt.android.dto

import kotlinx.serialization.Serializable

/**
 * @property login user login
 * @property password user password
 */
@Serializable
data class Credentials(
    val login: String,
    val password: String,
)
