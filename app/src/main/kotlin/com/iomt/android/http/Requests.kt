/**
 * File containing requests to backend
 */

package com.iomt.android.http

import android.util.Log
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.dto.Credentials
import com.iomt.android.dto.TokenInfo
import com.iomt.android.dto.UserData
import com.iomt.android.dto.UserDataWithId
import com.iomt.android.entities.*

import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable

internal const val BASE_URL = "https://iomt.lvk.cs.msu.ru"
internal const val API_V1 = "/api/v1"
internal const val API_V1_URL = "$BASE_URL$API_V1"
private val scope = CoroutineScope(Dispatchers.IO)

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val loggerTag = object {}.javaClass.enclosingClass.name

private val httpClient = createHttpClient(Android.create())

/**
 * @param substring substring to search
 * @return [List] of [DeviceConfig]s that contain [substring] in human-readable name from [DeviceConfig.general]'s
 */
suspend fun getDeviceTypes(substring: String): List<DeviceConfig> = httpClient.get(
    URLBuilder("$BASE_URL$API_V1/device_types").apply {
        if (substring.isNotBlank()) {
            parameters.append("name", substring)
        }
    }.build(),
).body()

/**
 * @return [UserData] corresponding to currently logged-in user
 */
suspend fun getUserData(): UserDataWithId = httpClient.get("$BASE_URL$API_V1/user").body<UserDataWithId>().also {
    RequestParams.userData = it
}

/**
 * @param userData [UserData] that should update the [UserData] stored on backend
 * @return [Unit]
 */
suspend fun sendUserData(userData: UserData): HttpStatusCode = httpClient.put("$BASE_URL$API_V1/user") {
    contentType(ContentType.Application.Json)
    setBody(userData)
}.let {
    if (it.status.isSuccess()) {
        val userId = RequestParams.userData?.id
        RequestParams.userData = userData.toUserDataWithId(userId!!)
    }
    return it.status
}

/**
 * @param userData registration [UserData]
 * @return true if Sign Up was successful, false otherwise
 */
suspend fun sendSignUpRequest(userData: UserData): HttpStatusCode = httpClient.post("$BASE_URL$API_V1/user") {
    contentType(ContentType.Application.Json)
    setBody(userData)
}.status

/**
 * @param signUpInfo
 * @param errorAction
 * @param successAction
 */
@Suppress("TOO_MANY_PARAMETERS")
fun sendReg(
    signUpInfo: SignUpInfo,
    errorAction: () -> Unit,
    successAction: (String?) -> Unit,
) {
    /**
     * @property error
     */
    @Serializable data class ErrorBody(val error: String?)

    val url = "$BASE_URL/users/register/"

    scope.launch {
        val response = httpClient.post {
            url(url)
            contentType(ContentType.Application.Json)
            setBody(signUpInfo)
        }
        if (response.status.isSuccess()) {
            Log.d(loggerTag, "Request <$url> was successfully sent.")
            val error: ErrorBody = response.body()
            successAction(error.error)
        } else {
            Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
            errorAction()
        }
    }
}

/**
 * @param login
 * @param password
 * @param callback callback that will be invoked on success
 * @return [TokenInfo]
 */
fun sendLogin(login: String, password: String, callback: suspend (TokenInfo?) -> Unit) {
    RequestParams.credentials = Credentials(login, password)
    scope.launch {
        try {
            callback(httpClient.authenticate())
        } catch (exception: ClientRequestException) {
            RequestParams.logout()
            callback(null)
        }
    }
}
