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

/**
 * Parse [dotFormattedDateString] into [LocalDate].
 *
 * @param dotFormattedDateString date string with format `dd.MM.yyyy`
 * @return [LocalDate] made of [dotFormattedDateString]
 */
fun LocalDate.Companion.parseFromDotFormat(dotFormattedDateString: String): LocalDate {
    val (days, months, years) = dotFormattedDateString.split(".").map { it.toInt() }
    return LocalDate(years, months, days)
}

/**
 * Dummy birthdate validation.
 *
 * [birthdate] is considered to be valid if it is less than current [LocalDateTime].
 *
 * As the format is pretty simple, and it is filled with DatePicker, we can simply split the string by dots and try to
 * compare days, months and years.
 *
 * @param birthdate date of birth in format DD.MM.YYYY
 * @return true if [birthdate] is a valid birthdate string, false otherwise
 */
fun isBirthdateValidFun(birthdate: String): Boolean {
    val now = LocalDateTime.now().date
    val (days, months, years) = try {
        birthdate.split(".").map { it.toInt() }
    } catch (exception: NumberFormatException) {
        return false
    }
    with(now) {
        return years < year || years == year && months < monthNumber || years == year && months == monthNumber && days < dayOfMonth
    }
}
