package com.iomt.android.configs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @property prettyName human-readable characteristic name
 * @property serviceUuid BLE UUID of service that is connected with characteristic
 * @property characteristicUuid BLE UUID of characteristic
 */
@Serializable
data class CharacteristicConfig(
    @SerialName("name") val prettyName: String,
    @SerialName("service_uuid") var serviceUuid: String? = null,
    @SerialName("characteristic_uuid") var characteristicUuid: String? = null,
) {
    companion object {
        val stub = CharacteristicConfig(
            "Heart Rate",
            "0000180d-0000-1000-8000-00805f9b34fb",
            "00002a37-0000-1000-8000-00805f9b34fb",
        )
    }
}
