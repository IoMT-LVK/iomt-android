/**
 * File containing HttpClient definition
 */

package com.iomt.android.http

import android.util.Log
import com.iomt.android.dto.Credentials
import com.iomt.android.dto.TokenInfo
import com.iomt.android.dto.UserDataWithId
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val REFRESH_TOKEN_URL = "$API_V1_URL/auth/user"

object RequestParams {
    /**
     * login and password required for jwt token renewal
     */
    var credentials: Credentials? = null

    /**
     * [UserDataWithId] corresponding to currently logged-in user
     */
    var userData: UserDataWithId? = null

    /**
     * Forget all the session connected info
     */
    fun logout() {
        credentials = null
        userData = null
    }
}

/**
 * @param url auth url, [REFRESH_TOKEN_URL] by default
 * @param additionalRequestBuilder additional [HttpRequestBuilder] lambda
 * @return [TokenInfo]
 * @throws ClientRequestException on failed authorization
 */
suspend fun HttpClient.authenticate(
    url: String = REFRESH_TOKEN_URL,
    additionalRequestBuilder: HttpRequestBuilder.() -> Unit = { },
): TokenInfo {
    val response = post(url) {
        additionalRequestBuilder()
        contentType(ContentType.Application.Json)
        RequestParams.credentials?.let {
            basicAuth(it.login, it.password)
        }
        setBody(Json.encodeToString(RequestParams.credentials))
    }
    return if (!response.status.isSuccess()) {
        throw ClientRequestException(response, "Authentication failed")
    } else {
        response.body()
    }
}

/**
 * @param engine [HttpClientEngine] to use
 * @param authUrl url auth url, [REFRESH_TOKEN_URL] by default
 * @param logMessage method to log the message, [Log.d] by default
 * @return [HttpClient] based on [engine]
 */
internal fun createHttpClient(
    engine: HttpClientEngine,
    authUrl: String = REFRESH_TOKEN_URL,
    logMessage: (String) -> Unit = { Log.d("HttpClient", it) },
) = HttpClient(engine) {
    install(ContentNegotiation) { json() }
    install(Logging) {
        level = LogLevel.INFO
        logger = object : Logger {
            override fun log(message: String) {
                logMessage(message)
            }
        }
    }
    install(Auth) {
        bearer {
            refreshTokens {
                RequestParams.credentials?.let {
                    val tokenInfo = client.authenticate(authUrl) { markAsRefreshTokenRequest() }
                    // todo: support refresh token
                    BearerTokens(tokenInfo.token, "")
                } ?: throw IllegalStateException("No credentials are provided")
            }
            sendWithoutRequest { request ->
                // should not authorize for `/auth/` and `/register/` endpoints
                request.url.pathSegments.contains("auth") || request.url.pathSegments.contains("register")
            }
        }
    }
}
