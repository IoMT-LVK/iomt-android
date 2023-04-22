package com.iomt.android.mqtt

/**
 * @property userId id of currently logged-in user
 * @property deviceMac MAC address of Bluetooth LE device
 * @property characteristicName name of a gatt characteristic
 */
data class Topic(
    val userId: Long,
    val deviceMac: String,
    val characteristicName: String,
) {
    /**
     * Topic name format:
     *   `/{user_id}/{mac_address}/{characteristic_name}`
     *
     * @return topic name
     */
    fun toTopicName() = "c/$userId/$deviceMac/$characteristicName"
}
