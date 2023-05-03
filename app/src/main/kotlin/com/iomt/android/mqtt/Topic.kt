package com.iomt.android.mqtt

/**
 * Data class that represents MQTT topic
 *
 * @property userId id of currently logged-in user
 * @property deviceMac MAC address of Bluetooth LE device
 */
data class Topic(
    val userId: Long,
    val deviceMac: String,
) {
    /**
     * Topic name format:
     *   `c/{user_id}/{mac_address}`
     *
     * @return topic name
     */
    fun toTopicName() = "c/$userId/$deviceMac"
}
