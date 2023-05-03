package com.iomt.android.http

import com.iomt.android.dto.Credentials
import com.iomt.android.dto.TokenInfo
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HttpClientTest {
    private val stubJwt = "vertolet"
    private val stubCredentials = Credentials("JohnDoe", "pwd")
    private val stubTokenInfo = TokenInfo(token = stubJwt, expires = "")
    private val mockEngine = MockEngine { request ->
        val isAuth = request.url.pathSegments.contains("auth")
        val prettyPathSegments = request.url.pathSegments.joinToString("/")
        println("$prettyPathSegments: Authorization = [${request.headers["Authorization"]}]")
        when {
             isAuth && request.body.toString().contains("\"login\":\"${stubCredentials.login}\"") -> respond(
                 content = Json.encodeToString(stubTokenInfo),
                 status = HttpStatusCode.OK,
                 headers = headersOf(HttpHeaders.ContentType, "application/json"),
             )
            isAuth -> respondError(HttpStatusCode.Forbidden)
            request.headers["Authorization"] == "Bearer $stubJwt" -> respondOk()
            else -> respondError(HttpStatusCode.Unauthorized)
        }
    }
    private var myClient: HttpClient? = null

    @BeforeEach
    fun beforeTest() {
        myClient = createHttpClient(mockEngine, "$API_V1/auth/user") {
            println(it)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should authenticate automatically`() = runTest {
        RequestParams.credentials = stubCredentials
        val response = myClient?.get("$API_V1/test")
        assertEquals(HttpStatusCode.OK, response?.status)
        assertEquals(3, (myClient?.engine as MockEngine).requestHistory.count())
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `should not authenticate automatically on auth`() = runTest {
        RequestParams.credentials = stubCredentials
        val tokenInfo = myClient?.authenticate("$API_V1/auth/user")
        assertEquals(stubJwt, tokenInfo?.token)
        assertEquals(1, (myClient?.engine as MockEngine).requestHistory.count())
    }
}
