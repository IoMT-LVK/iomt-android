package com.iomt.android.credentialmanager

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.*
import com.iomt.android.dto.Credentials
import com.iomt.android.dto.toCredentials

/**
 * [CredentialManager] wrapper
 */
class AppCredentialManager(private val context: Context) {
    private val credentialManager by lazy { CredentialManager.create(context) }
    private val activity = context as Activity
    private var cache: Credentials? = null

    /**
     * @param credentials [Credentials] to save
     */
    suspend fun createIfNotPresent(credentials: Credentials) {
        cache ?: create(credentials)
    }

    /**
     * @param credentials [Credentials] to save
     */
    private suspend fun create(credentials: Credentials) {
        try {
            credentialManager.createCredential(
                request = CreatePasswordRequest(credentials.login, credentials.password),
                activity = activity,
            )
            Log.i(loggerTag, "Credentials successfully created.")
        } catch (exception: CreateCredentialCancellationException) {
            // User chose not to save credentials
            Log.w(loggerTag, "User cancelled credentials save.")
        } catch (exception: CreateCredentialException) {
            Log.e(loggerTag, "Credential save error", exception)
        }
    }

    /**
     * @return saved [Credentials]
     */
    suspend fun get(): Credentials? {
        val getCredentialsRequest = GetCredentialRequest(
            listOf(GetPasswordOption()),
        )

        return try {
            credentialManager.getCredential(
                request = getCredentialsRequest,
                activity = context as Activity,
            ).toCredentials().also { cache = it }
        } catch (exception: GetCredentialCancellationException) {
            // user cancelled the request
            Log.w(loggerTag, "User cancelled credential request.", exception)
            null
        } catch (exception: NoCredentialException) {
            // no saved credentials
            Log.i(loggerTag, "No saved credential.", exception)
            null
        } catch (exception: GetCredentialException) {
            Log.e(loggerTag, "Error getting credential.", exception)
            null
        }
    }

    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = object { }.javaClass.enclosingClass.name
    }
}
