package com.iomt.android.configs

import com.iomt.android.R
import kotlinx.serialization.SerialName

/**
 * @property iconId
 */
enum class DeviceType(val iconId: Int) {
    @SerialName("bracelet") BRACELET(R.drawable.band_icon),
    UNKNOWN(R.drawable.default_device),
    @SerialName("vest") VEST(R.drawable.vest_icon),
    ;
}
