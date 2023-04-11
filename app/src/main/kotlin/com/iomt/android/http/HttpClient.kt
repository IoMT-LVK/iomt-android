/**
 * File containing HttpClient definition
 */

package com.iomt.android.http

import android.util.Log
import com.iomt.android.dto.Credentials
import com.iomt.android.entities.AuthInfo
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

private const val REFRESH_TOKEN_URL = "$BASE_URL/auth/"

object RequestParams {
    /**
     * login and password required for jwt token renewal
     */
    var credentials: Credentials? = null

    /**
     * ID of a current user
     * todo: remove when backend is able to get userId from jwt token
     */
    var userId: String? = null

    /**
     * Forget all the session connected info
     */
    fun logout() {
        credentials = null
        userId = null
    }
}

/**
 * @param url auth url, [REFRESH_TOKEN_URL] by default
 * @param additionalRequestBuilder additional [HttpRequestBuilder] lambda
 * @return [AuthInfo]
 * @throws ClientRequestException on failed authorization
 */
suspend fun HttpClient.authenticate(
    url: String = REFRESH_TOKEN_URL,
    additionalRequestBuilder: HttpRequestBuilder.() -> Unit = { }
): AuthInfo {
    val response = post(url) {
        additionalRequestBuilder()
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(RequestParams.credentials))
    }
    if (!response.status.isSuccess()) {
        throw ClientRequestException(response, "Authentication failed")
    }
    val authInfo: AuthInfo = response.body()
    if (authInfo.jwt.isNotBlank()) {
        return authInfo
    } else {
        throw ClientRequestException(response, "Authentication failed")
    }
}

/**
 * @param engine [HttpClientEngine] to use
 * @param authUrl url auth url, [REFRESH_TOKEN_URL] by default
 * @return [HttpClient] based on [engine]
 */
internal fun createHttpClient(engine: HttpClientEngine, authUrl: String = REFRESH_TOKEN_URL) = HttpClient(engine) {
    install(ContentNegotiation) { json() }
    install(Logging) {
        level = LogLevel.HEADERS
        logger = object : Logger {
            override fun log(message: String) {
                Log.d("HttpClient", message)
            }
        }
    }
    install(Auth) {
        bearer {
            refreshTokens {
                RequestParams.credentials?.let {
                    val authInfo = client.authenticate(authUrl) { markAsRefreshTokenRequest() }
                    // todo: support refresh token
                    BearerTokens(authInfo.jwt, "")
                } ?: throw IllegalStateException("No credentials are provided")
            }
            sendWithoutRequest { request ->
                // should not authorize for `/auth/` and `/register/` endpoints
                request.url.pathSegments.contains("auth") || request.url.pathSegments.contains("register")
            }
        }
    }
}
