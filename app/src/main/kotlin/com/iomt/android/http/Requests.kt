/**
 * File containing requests to backend
 */

package com.iomt.android.http

import android.util.Log
import com.iomt.android.configs.DeviceConfig
import com.iomt.android.dto.Credentials
import com.iomt.android.entities.*

import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val BASE_URL = "https://iomt.lvk.cs.msu.ru"
internal const val API_V1 = "/api/v1"
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
        parametersOf("name", substring)
    }.build()
).body()

/**
 * @param device [DeviceInfo] that will be sent
 */
fun sendDevice(device: DeviceInfo) {
    val url = "$BASE_URL/devices/register/?user_id=${RequestParams.userId}"
    scope.launch {
        val response = httpClient.post {
            url(url)
            contentType(ContentType.Application.Json)
            setBody(device)
        }
        if (response.status.isSuccess()) {
            Log.d(loggerTag, "Request <$url> was successfully sent.")
        } else {
            Log.e(loggerTag, "Got ${response.status} from server with <$url>.")
        }
    }
}

/**
 * @param device [DeviceInfo] of device that will be deleted
 */
fun deleteDevice(device: DeviceInfo) {
    val url = "$BASE_URL/devices/delete/?user_id=${RequestParams.userId}&id=${device.address}"
    scope.launch {
        val response = httpClient.get {
            url(url)
        }
        if (response.status.isSuccess()) {
            Log.d(loggerTag, "Request <$url> was successfully sent.")
        } else {
            Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
        }
    }
}

/**
 * @param resultCallback callback that should be invoked on success
 */
fun getDeviceTypes(resultCallback: (List<DeviceType>) -> Unit) {
    /**
     * @property deviceList
     */
    @Serializable data class DeviceTypeList(val deviceList: List<DeviceType>)

    val url = "$BASE_URL/devices/types/?user_id=${RequestParams.userId}"

    scope.launch {
        val response = httpClient.get {
            url(url)
        }
        if (response.status.isSuccess()) {
            Log.d(loggerTag, "Request <$url> was successfully sent.")
            val devices: DeviceTypeList = response.body()
            resultCallback(devices.deviceList)
        } else {
            Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
        }
    }
}

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
 * @param resultCallback callback that should be invoked on success
 */
fun getData(resultCallback: (UserData) -> Unit) {
    val url = "$BASE_URL/users/info/?user_id=${RequestParams.userId}"

    scope.launch {
        val response = httpClient.get {
            url(url)
        }
        if (response.status.isSuccess()) {
            Log.d(loggerTag, "Request <$url> was successfully sent.")
            val userData: UserData = response.body()
            resultCallback(userData)
        } else {
            Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
        }
    }
}

/**
 * @param name
 * @param surname
 * @param patronymic
 * @param birthdate
 * @param email
 * @param phone
 * @param weight
 * @param height
 */
@Suppress("TOO_MANY_PARAMETERS")
fun sendData(
    name: String?,
    surname: String?,
    patronymic: String?,
    birthdate: String?,
    email: String?,
    phone: String?,
    weight: Int,
    height: Int
) {
    /**
     * @property name
     * @property surname
     * @property birthdate
     * @property height
     * @property weight
     * @property email
     * @property phoneNumber
     * @property patronymic
     */
    @Serializable
    data class UserDataDto(
        val name: String?,
        val surname: String?,
        val birthdate: String?,
        val height: Int,
        val weight: Int,
        val email: String?,
        @SerialName("phone_number") val phoneNumber: String?,
        val patronymic: String?,
    )

    val url = "$BASE_URL/users/info/?user_id=${RequestParams.userId}"

    scope.launch {
        val response = httpClient.post {
            url(url)
            contentType(ContentType.Application.Json)
            setBody(
                UserDataDto(
                    name,
                    surname,
                    birthdate,
                    height,
                    weight,
                    email,
                    phone,
                    patronymic,
                )
            )
        }
        if (response.status.isSuccess()) {
            Log.d(loggerTag, "Request <$url> was successfully sent.")
        } else {
            Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
        }
    }
}

/**
 * @param configId
 * @param callback
 * @return [String] of device config
 * @throws IllegalStateException
 */
fun getDeviceConfig(configId: Long, callback: (String) -> Unit) {
    val url = "$BASE_URL/devices/types/?id=$configId"
    Log.d(loggerTag, "Fetching config with id $configId")
    scope.launch {
        val response = httpClient.get {
            url(url)
        }
        if (response.status.isSuccess()) {
            callback(response.bodyAsText())
        } else {
            Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
            throw IllegalStateException("Could not get config $configId")
        }
    }
}

/**
 * @param login
 * @param password
 * @param callback callback that will be invoked on success
 * @return [AuthInfo]
 */
fun sendLogin(login: String, password: String, callback: (AuthInfo) -> Unit) {
    RequestParams.credentials = Credentials(login, password)
    scope.launch {
        try {
            callback(httpClient.authenticate())
        } catch (exception: ClientRequestException) {
            RequestParams.logout()
            callback(AuthInfo.empty)
        }
    }
}
