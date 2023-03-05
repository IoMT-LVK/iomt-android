package com.iomt.android

import android.util.Log
import com.iomt.android.entities.*

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val httpClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}
private val scope = CoroutineScope(Dispatchers.IO)

/**
 * Class that is used to perform requests.
 */
class Requests(
    private var jwt: String? = null,
    private var userId: String? = null,
) {
    /**
     * @param device [DeviceInfo] that will be sent
     */
    fun sendDevice(device: DeviceInfo) {
        val url = "$BASE_URL/devices/register/?token=$jwt&user_id=$userId"
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
        val url = "$BASE_URL/devices/delete/?token=$jwt&user_id=$userId&id=${device.address}"
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
    fun getDevices(resultCallback: (List<DeviceInfo>) -> Unit) {
        /**
         * @property devices
         */
        @Serializable
        data class DeviceList(val devices: List<DeviceInfo>)

        Log.w(loggerTag, "JWT: $jwt, userID: $userId")
        val url = "$BASE_URL/devices/get/?token=$jwt&user_id=$userId"

        scope.launch {
            val response = httpClient.get {
                url(url)
            }

            if (response.status.isSuccess()) {
                Log.d(loggerTag, "Request <$url> was successfully sent.")
                val devices: DeviceList = response.body()
                resultCallback(devices.devices)
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

        val url = "$BASE_URL/devices/types/?token=$jwt&user_id=$userId"

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
     * @param login
     * @param password
     * @param callback callback that will be invoked on success
     * @return [AuthInfo]
     */
    fun sendLogin(login: String, password: String, callback: (AuthInfo) -> Unit) {
        /**
         * @property login
         * @property password
         */
        @Serializable data class Credentials(val login: String, val password: String)
        val url = "$BASE_URL/auth/"

        scope.launch {
            val response = httpClient.post {
                url(url)
                contentType(ContentType.Application.Json)
                setBody(Credentials(login, password))
            }
            if (response.status.isSuccess()) {
                val authInfo: AuthInfo = response.body()
                callback(authInfo)
            } else {
                Log.e(loggerTag, "Got [${response.status}] from server with <$url>.")
                callback(AuthInfo.empty)
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
        val url = "$BASE_URL/users/info/?token=$jwt&user_id=$userId"

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

        val url = "$BASE_URL/users/info/?token=$jwt&user_id=$userId"

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
        val url = "$BASE_URL/devices/types/?id=$configId&token=$jwt"
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

    companion object {
        private const val BASE_URL = "https://iomt.lvk.cs.msu.ru"
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val loggerTag = object {}.javaClass.enclosingClass.name
    }
}
