package com.iomt.android.config.configs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property name characteristic name
 * @property serviceUuid BLE UUID of service that is connected with characteristic
 * @property characteristicUuid BLE UUID of characteristic
 * @property descriptorUuid BLE UUID of characteristic's descriptor
 */
@Serializable
data class CharacteristicConfig(
    var name: String,
    @SerialName("serviceUUID") var serviceUuid: String? = null,
    @SerialName("characteristicUUID") var characteristicUuid: String? = null,
    @SerialName("descriptorUUID") var descriptorUuid: String? = null,
) {
    companion object {
        val stub = CharacteristicConfig("heartRate")
    }
}
