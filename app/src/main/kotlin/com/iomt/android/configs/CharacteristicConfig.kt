package com.iomt.android.configs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @property name characteristic name in camelcase
 * @property prettyName human-readable characteristic name // TODO: remove Transient annotation when server is updated
 * @property serviceUuid BLE UUID of service that is connected with characteristic
 * @property characteristicUuid BLE UUID of characteristic
 */
@Serializable
data class CharacteristicConfig(
    val name: String,
    @Transient val prettyName: String = "REPLACE ME",
    @SerialName("service_uuid") var serviceUuid: String? = null,
    @SerialName("sensor_uuid") var characteristicUuid: String? = null,
) {
    companion object {
        val stub = CharacteristicConfig(
            "heartRate",
            "Heart Rate",
            "0000180d-0000-1000-8000-00805f9b34fb",
            "00002a37-0000-1000-8000-00805f9b34fb",
        )
    }
}
