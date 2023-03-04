@file:Suppress("COMMENT_WHITE_SPACE", "MISSING_KDOC_CLASS_ELEMENTS", "MISSING_KDOC_ON_FUNCTION")

package com.iomt.android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.iomt.android.utils.getDataOrThrowException
import org.json.JSONObject

/**
 * Class responsible for database interactions
 */
class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) = db.execSQL(Note.createTable())
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = db.execSQL("DROP TABLE IF EXISTS ${Note.TABLE_NAME}").also { onCreate(db) }

    /**
     * @param data data to insert
     * @return id of a newly created record
     */
    fun insertNote(data: JSONObject): Long {
        writableDatabase.use { db ->
            val values = ContentValues()
            try {
                values.put(Note.COLUMN_HEART_RATE, data.getInt(Note.COLUMN_HEART_RATE))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_HEART_RATE)
            }
            try {
                values.put(Note.COLUMN_RESP_RATE, data.getInt(Note.COLUMN_RESP_RATE))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_RESP_RATE)
            }
            try {
                values.put(Note.COLUMN_INSP, data.getDouble(Note.COLUMN_INSP))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_INSP)
            }
            try {
                values.put(Note.COLUMN_EXP, data.getDouble(Note.COLUMN_EXP))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_EXP)
            }
            try {
                values.put(Note.COLUMN_CADENCE, data.getInt(Note.COLUMN_CADENCE))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_CADENCE)
            }
            try {
                values.put(Note.COLUMN_STEP_COUNT, data.getInt(Note.COLUMN_STEP_COUNT))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_STEP_COUNT)
            }
            try {
                values.put(Note.COLUMN_ACT, data.getDouble(Note.COLUMN_ACT))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_ACT)
            }
            try {
                values.put(Note.COLUMN_CLI, data.getString(Note.COLUMN_CLI))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_CLI)
            }
            try {
                values.put(Note.COLUMN_MIL, data.getInt(Note.COLUMN_MIL))
            } catch (ex: Exception) {
                values.putNull(Note.COLUMN_MIL)
            }
            return db.insert(Note.TABLE_NAME, null, values)
        }
    }

    /**
     * @param id id of a note
     * @return note by [id]
     */
    fun getNote(id: Long): Map<String, String> = readableDatabase.use { db ->
        db.query(
            Note.TABLE_NAME,
            null,
            Note.COLUMN_ID + "=?", arrayOf(id.toString()), null, null, null, null
        )
            .also { it.moveToFirst() }
            .use { cursor ->
                sequenceOf(
                    Note.COLUMN_HEART_RATE,
                    Note.COLUMN_RESP_RATE,
                    Note.COLUMN_INSP,
                    Note.COLUMN_EXP,
                    Note.COLUMN_CADENCE,
                    Note.COLUMN_STEP_COUNT,
                    Note.COLUMN_ACT,
                    Note.COLUMN_CLI,
                    Note.COLUMN_MIL,
                )
                    .map { it to cursor.getDataOrThrowException(it) }
                    .toMap()
            }
    }

    /**
     * @return number of notes present in database
     */
    fun getNotesCount(): Int = readableDatabase.use { db -> db.rawQuery("SELECT * FROM ${Note.TABLE_NAME}", null).use { cursor -> cursor.count } }

    /**
     * @param id id of a note to delete
     * @return number of notes deleted
     */
    fun deleteNote(id: Int) = writableDatabase.use { it.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?", arrayOf(id.toString())) }

    /**
     * @return number of notes deleted
     */
    fun clear() = writableDatabase.use { it.delete(Note.TABLE_NAME, null, null) }

    companion object {
        private const val DATABASE_NAME = "notes_db"
        private const val DATABASE_VERSION = 1
    }
}
