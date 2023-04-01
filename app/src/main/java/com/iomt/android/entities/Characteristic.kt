package com.iomt.android.entities

import com.iomt.android.R

/**
 * @property name name of characteristic from config file
 * @property prettyName human-readable name of characteristic
 * @property value current value of characteristic
 */
data class Characteristic(
    val name: String,
    val prettyName: String,
    val value: String = DEFAULT_VALUE,
) {
    /**
     * Get icon for characteristic row depending on [Characteristic.name]
     *
     * @return icon id that should be displayed in characteristic row
     */
    fun getIcon() = when (name) {
        "heartRate" -> R.drawable.heart
        "inspRate", "inspirationRate" -> R.drawable.insp
        "expRate", "expirationRate" -> R.drawable.exp
        "steps", "stepsCount" -> R.drawable.steps
        "activity", "activityRate" -> R.drawable.act
        "cadence" -> R.drawable.cadence
        "battery", "batteryRate" -> R.drawable.battery
        else -> R.drawable.check
    }

    companion object {
        private const val DEFAULT_VALUE = "--"

        /**
         * Stub for testing
         */
        val stub = Characteristic("heartRate", "Heart Rate")
    }
}
