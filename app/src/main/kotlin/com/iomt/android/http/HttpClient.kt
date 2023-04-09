/**
 * File containing HttpClient definition
 */

package com.iomt.android.http

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

val updateCredentials: (Credentials) -> Unit = { credentials = it }

private var credentials: Credentials? = null

/**
 * @param url auth url, [REFRESH_TOKEN_URL] by default
 * @param additionalRequestBuilder additional [HttpRequestBuilder] lambda
 * @return [AuthInfo]
 * @throws ClientRequestException on failed authorization
 */
suspend fun HttpClient.authenticate(url: String = REFRESH_TOKEN_URL, additionalRequestBuilder: HttpRequestBuilder.() -> Unit = { }): AuthInfo {
    val response = post(url) {
        additionalRequestBuilder()
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(credentials))
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
        logger = Logger.DEFAULT
        level = LogLevel.HEADERS
    }
    install(Auth) {
        bearer {
            refreshTokens {
                credentials?.let {
                    val authInfo = client.authenticate(authUrl) { markAsRefreshTokenRequest() }
                    BearerTokens(authInfo.jwt, "")
                } ?: throw IllegalStateException("No credentials are provided")
            }
            sendWithoutRequest { request ->
                request.url.pathSegments.contains("auth")
            }
        }
    }
}
