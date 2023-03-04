/**
 * Utils for database interactions
 */

package com.iomt.android.utils

import android.database.Cursor
import org.json.JSONObject

/**
 * @param columnName name of a column to get
 * @return [String] value stored in column with [columnName]
 */
fun Cursor.getDataOrThrowException(columnName: String) = getDataOrNull(columnName) ?: throw IllegalStateException("Could not find column with name $columnName")

/**
 * @param columnName
 * @return index of column with [columnName] if column is present, null otherwise
 */
fun Cursor.getColumnIndexOrNull(columnName: String) = getColumnIndex(columnName).takeIf { it >= 0 }

/**
 * @param columnName
 * @return [String] data that is present in column with [columnName]
 */
fun Cursor.getDataOrNull(columnName: String) = getColumnIndexOrNull(columnName)?.let { getString(it) }

/**
 * @param index
 * @param defaultValue
 * @param getter
 */
fun <T : Any> Cursor.getOr(index: Int, defaultValue: Any = JSONObject.NULL, getter: Cursor.(Int) -> T) = if (isNull(index)) {
    defaultValue
} else {
    getter(index)
}

/**
 * @param columnName
 * @param getter
 * @return value present in column with name [columnName], gotten with [getter] or [JSONObject.NULL] if column is null
 */
fun <T : Any> Cursor.getValueByColumnName(columnName: String, getter: Cursor.(Int) -> T) = getOr(getColumnIndexOrThrow(columnName)) { getter(it) }
