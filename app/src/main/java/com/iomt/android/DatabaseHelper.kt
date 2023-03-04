@file:Suppress("COMMENT_WHITE_SPACE", "MISSING_KDOC_CLASS_ELEMENTS", "MISSING_KDOC_ON_FUNCTION")

package com.iomt.android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.ArrayMap
import android.util.Log
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
    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        // create notes table
        db.execSQL(Note.createTable())
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS ${Note.TABLE_NAME}")

        // Create tables again
        onCreate(db)
    }

    /**
     * @param data
     * @return id of a record
     */
    fun insertNote(data: JSONObject): Long {
        // get writable database as we want to write data
        val db = this.writableDatabase
        val values = ContentValues()
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
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

        // insert row
        val id = db.insert(Note.TABLE_NAME, null, values)

        // close db connection
        db.close()

        // return newly inserted row id
        return id
    }

    /**
     * @param id
     * @return note by [id]
     */
    fun getNote(id: Long): ArrayMap<String, String> {
        val db = this.readableDatabase
        val cursor = db.query(
            Note.TABLE_NAME,
            null,
            Note.COLUMN_ID + "=?", arrayOf(id.toString()), null, null, null, null
        )

        // prepare note object
        val result: ArrayMap<String, String> = ArrayMap()
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("ASdasd", cursor.toString())
            result["HeartRate"] =
                    cursor.getString(cursor.getColumnIndex(Note.COLUMN_HEART_RATE))
            result["RespRate"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_RESP_RATE))
            result["Insp"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_INSP))
            result["Exp"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_EXP))
            result["Cadence"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_CADENCE))
            result["Steps"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT))
            result["Activity"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_ACT))
            result["Clitime"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_CLI))
            result["Millisec"] = cursor.getString(cursor.getColumnIndex(Note.COLUMN_MIL))
            cursor.close()
        }
        // Note note = new Note(
        // //cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)),
        // result,
        // cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));
        // close the db connection
        db.close()
        return result
    }

    /* public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Note.TABLE_NAME + " ORDER BY " +
                Note.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
                note.setNote(cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)));
                note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));

                notes.add(note);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }*/ /*

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Note.COLUMN_NOTE, note.getNote());

        // updating row
        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }*/
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val notesCount: Int
        get() {
            val countQuery = "SELECT  * FROM ${Note.TABLE_NAME}"
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            val count = cursor.count
            cursor.close()
            return count
        }

    /**
     * @param id
     */
    fun deleteNote(id: Int) {
        val db = this.writableDatabase
        db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?", arrayOf(id.toString()))
        db.close()
    }

    fun clear() {
        // List<Note> notes = new ArrayList<>();

        // Select All Query
        val selectQuery = "DELETE FROM ${Note.TABLE_NAME}"
        val db = this.writableDatabase
        db.delete(Note.TABLE_NAME, null, null)
        // Cursor cursor = db.rawQuery(selectQuery, null);

        // if (cursor.moveToFirst()) {
        // do {
        // Note note = new Note();
        // note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
        // 
        // //                Map<String, String> result = new ArrayMap<>();
        // //                try {
        // //                    result.put("HeartRate", cursor.getString(cursor.getColumnIndex(Note.COLUMN_HEART_RATE)));
        // //                    result.put("RespRate", cursor.getString(cursor.getColumnIndex(Note.COLUMN_RESP_RATE)));
        // //                    result.put("Insp", cursor.getString(cursor.getColumnIndex(Note.COLUMN_INSP)));
        // //                    result.put("Exp", cursor.getString(cursor.getColumnIndex(Note.COLUMN_EXP)));
        // //                    result.put("Cadence", cursor.getString(cursor.getColumnIndex(Note.COLUMN_CADENCE)));
        // //                    result.put("Steps",  cursor.getString(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT)));
        // //                    result.put("Activity", cursor.getString(cursor.getColumnIndex(Note.COLUMN_ACT)));
        // //                    result.put("Clitime", cursor.getString(cursor.getColumnIndex(Note.COLUMN_CLI)));
        // //                } catch (Exception ex) {
        // //
        // //                }
        // //                note.setNote(result);
        // //                note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));
        // 
        // db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?",
        // new String[]{String.valueOf(note.getId())});
        // } while (cursor.moveToNext());
        // }

        // close db connection
        // cursor.close();
        db.close()
    }

    companion object {
        // Database Name
        private const val DATABASE_NAME = "notes_db"

        // Database Version
        private const val DATABASE_VERSION = 1
    }
}
