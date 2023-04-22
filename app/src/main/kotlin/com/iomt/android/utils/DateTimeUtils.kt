/**
 * File containing utility functions for kotlinx.datetime
 */

package com.iomt.android.utils

import kotlin.time.Duration
import kotlinx.datetime.*

/**
 * @return [LocalDateTime] object filled with current system date, time and timezone
 */
fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * @param duration [Duration] that should be subtracted from current datetime as [LocalDateTime]
 * @return [LocalDateTime] that used to be [duration] ago
 */
fun LocalDateTime.Companion.beforeNow(duration: Duration) = Clock.System.now()
    .minus(duration)
    .toLocalDateTime(TimeZone.currentSystemDefault())
