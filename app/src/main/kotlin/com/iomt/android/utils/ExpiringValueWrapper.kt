package com.iomt.android.utils

import kotlinx.datetime.LocalDateTime

/**
 * A wrapper around a value of type [T] that caches it until expiration time hasn't come and then recalculates
 * using [valueGetter]
 *
 * @property valueGetter a function to calculate the value of type [T] and its expiration time
 */
class ExpiringValueWrapper<T : Any>(
    private val valueGetter: suspend () -> Pair<T, LocalDateTime>,
) {
    private var expirationDateTime: LocalDateTime? = null
    private var value: T? = null

    private suspend fun updateValue() {
        val (newValue, newExpirationDateTime) = valueGetter()
        value = newValue
        expirationDateTime = newExpirationDateTime
    }

    /**
     * @return cached value or refreshes the value and returns it
     */
    suspend fun getValue(): T {
        val current = LocalDateTime.now()
        expirationDateTime?.let {
            if (it > current) {
                updateValue()
            }
        } ?: updateValue()

        return value!!
    }
}
