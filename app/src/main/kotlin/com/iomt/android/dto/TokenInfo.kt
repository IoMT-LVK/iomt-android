package com.iomt.android.dto

import kotlinx.serialization.Serializable

/**
 * @property expires expiration datetime in format `YYYY-MM-DDThh:mm:ssZ`
 * @property token jwt-token received from backend
 */
@Serializable
data class TokenInfo(
    val expires: String,
    val token: String,
)
