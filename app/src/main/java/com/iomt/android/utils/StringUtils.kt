/**
 * String utils
 */

package com.iomt.android.utils

/**
 * @return true if all the characters in this [String] are letters, false otherwise
 */
fun String.isLetters() = all { it.isLetter() }
