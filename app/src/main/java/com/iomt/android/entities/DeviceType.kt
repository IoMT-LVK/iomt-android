package com.iomt.android.entities

import kotlinx.serialization.Serializable

/**
 * @property deviceType
 * @property prefix
 */
@Serializable
data class DeviceType(val deviceType: String, val prefix: String)
