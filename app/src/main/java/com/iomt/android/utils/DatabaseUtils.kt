/**
 * Utils for database interactions
 */

package com.iomt.android.utils

import android.database.Cursor

/**
 * @param columnName name of a column to get
 * @return [String] value stored in column with [columnName]
 */
fun Cursor.getDataOrThrowException(columnName: String) = getColumnIndex(columnName).takeIf { it >= 0 }
    ?.let { getString(it) } ?: throw IllegalStateException("Could not find column with name $columnName")
