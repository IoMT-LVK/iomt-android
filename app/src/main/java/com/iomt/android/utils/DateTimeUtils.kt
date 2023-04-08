/**
 * File containing utility functions for kotlinx.datetime
 */

package com.iomt.android.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * @return [LocalDateTime] object filled with current system date, time and timezone
 */
fun LocalDateTime.Companion.now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
