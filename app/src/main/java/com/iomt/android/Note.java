package com.iomt.android;

import java.util.Map;

public class Note {
    public static final String TABLE_NAME = "hexoskin_data";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_HEART_RATE = "HeartRate";
    public static final String COLUMN_RESP_RATE = "RespRate";
    public static final String COLUMN_INSP = "Insp";
    public static final String COLUMN_EXP = "Exp";
    public static final String COLUMN_CADENCE = "Cadence";
    public static final String COLUMN_STEP_COUNT = "Steps";
    public static final String COLUMN_CLI = "Clitime";
    public static final String COLUMN_ACT = "Activity";
    public static final String COLUMN_MIL = "Millisec";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private Map<String, String> note;
    private String timestamp;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_HEART_RATE + " INTEGER,"
                    + COLUMN_RESP_RATE + " INTEGER,"
                    + COLUMN_INSP + " FLOAT,"
                    + COLUMN_EXP + " FLOAT,"
                    + COLUMN_CADENCE + " INTEGER,"
                    + COLUMN_STEP_COUNT + " INTEGER,"
                    + COLUMN_ACT + " FLOAT,"
                    + COLUMN_CLI + " TEXT,"
                    + COLUMN_MIL + " INTEGER,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public Note() {
    }

    public Note(int id, Map<String, String> note, String timestamp) {
        this.id = id;
        this.note = note;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getNote() {
        return note;
    }

    public void setNote(Map<String, String> note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
