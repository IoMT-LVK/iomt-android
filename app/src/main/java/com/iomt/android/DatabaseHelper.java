package com.iomt.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notes_db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(Note.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Note.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertNote(JSONObject data) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        try {
            values.put(Note.COLUMN_HEART_RATE, data.getInt(Note.COLUMN_HEART_RATE));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_HEART_RATE);
        }
        try {
            values.put(Note.COLUMN_RESP_RATE, data.getInt(Note.COLUMN_RESP_RATE));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_RESP_RATE);
        }
        try {
            values.put(Note.COLUMN_INSP, data.getDouble(Note.COLUMN_INSP));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_INSP);
        }
        try {

            values.put(Note.COLUMN_EXP, data.getDouble(Note.COLUMN_EXP));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_EXP);
        }
        try {
            values.put(Note.COLUMN_CADENCE, data.getInt(Note.COLUMN_CADENCE));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_CADENCE);
        }
        try {
            values.put(Note.COLUMN_STEP_COUNT, data.getInt(Note.COLUMN_STEP_COUNT));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_STEP_COUNT);
        }
        try {
            values.put(Note.COLUMN_ACT, data.getDouble(Note.COLUMN_ACT));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_ACT);
        }
        try {
            values.put(Note.COLUMN_CLI, data.getString(Note.COLUMN_CLI));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_CLI);
        }
        try {
            values.put(Note.COLUMN_MIL, data.getInt(Note.COLUMN_MIL));
        } catch (Exception ex) {
            values.putNull(Note.COLUMN_MIL);
        }

        // insert row
        long id = db.insert(Note.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public ArrayMap<String, String> getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Note.TABLE_NAME,
                null,
                Note.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);


        // prepare note object
        ArrayMap<String, String> result = new ArrayMap<>();
        if (cursor != null && cursor.moveToFirst()) {
            Log.d("ASdasd", cursor.toString());
            result.put("HeartRate", cursor.getString(cursor.getColumnIndex(Note.COLUMN_HEART_RATE)));
            result.put("RespRate", cursor.getString(cursor.getColumnIndex(Note.COLUMN_RESP_RATE)));
            result.put("Insp", cursor.getString(cursor.getColumnIndex(Note.COLUMN_INSP)));
            result.put("Exp", cursor.getString(cursor.getColumnIndex(Note.COLUMN_EXP)));
            result.put("Cadence", cursor.getString(cursor.getColumnIndex(Note.COLUMN_CADENCE)));
            result.put("Steps", cursor.getString(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT)));
            result.put("Activity", cursor.getString(cursor.getColumnIndex(Note.COLUMN_ACT)));
            result.put("Clitime", cursor.getString(cursor.getColumnIndex(Note.COLUMN_CLI)));
            result.put("Millisec", cursor.getString(cursor.getColumnIndex(Note.COLUMN_MIL)));
            cursor.close();
        }
//        Note note = new Note(
//                //cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)),
//                result,
//                cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));
        // close the db connection
        db.close();

        return result;
    }

    /*public List<Note> getAllNotes() {
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
    }*/

    public int getNotesCount() {
        String countQuery = "SELECT  * FROM " + Note.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }/*

    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Note.COLUMN_NOTE, note.getNote());

        // updating row
        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
    }*/

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void clear() {
        //List<Note> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "DELETE FROM " + Note.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Note.TABLE_NAME, null, null);
        //Cursor cursor = db.rawQuery(selectQuery, null);

//        if (cursor.moveToFirst()) {
//            do {
//                Note note = new Note();
//                note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
//
////                Map<String, String> result = new ArrayMap<>();
////                try {
////                    result.put("HeartRate", cursor.getString(cursor.getColumnIndex(Note.COLUMN_HEART_RATE)));
////                    result.put("RespRate", cursor.getString(cursor.getColumnIndex(Note.COLUMN_RESP_RATE)));
////                    result.put("Insp", cursor.getString(cursor.getColumnIndex(Note.COLUMN_INSP)));
////                    result.put("Exp", cursor.getString(cursor.getColumnIndex(Note.COLUMN_EXP)));
////                    result.put("Cadence", cursor.getString(cursor.getColumnIndex(Note.COLUMN_CADENCE)));
////                    result.put("Steps",  cursor.getString(cursor.getColumnIndex(Note.COLUMN_STEP_COUNT)));
////                    result.put("Activity", cursor.getString(cursor.getColumnIndex(Note.COLUMN_ACT)));
////                    result.put("Clitime", cursor.getString(cursor.getColumnIndex(Note.COLUMN_CLI)));
////                } catch (Exception ex) {
////
////                }
////                note.setNote(result);
////                note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));
//
//                db.delete(Note.TABLE_NAME, Note.COLUMN_ID + " = ?",
//                        new String[]{String.valueOf(note.getId())});
//            } while (cursor.moveToNext());
//        }

        // close db connection
        //cursor.close();
        db.close();
    }
}