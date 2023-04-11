package com.iomt.android.dto

import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
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

/**
 * @return [Credentials] from [GetCredentialResponse]
 */
fun GetCredentialResponse.toCredentials(): Credentials? = when (credential) {
    is PasswordCredential -> {
        val passwordCredentials = credential as PasswordCredential
        Credentials(passwordCredentials.id, passwordCredentials.password)
    }
    else -> null
}
