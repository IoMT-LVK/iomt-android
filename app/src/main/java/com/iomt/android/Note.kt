package com.iomt.android

class Note {
    var id = 0
    var note: Map<String, String>? = null
    var timestamp: String? = null

    constructor() {}
    constructor(id: Int, note: Map<String, String>?, timestamp: String?) {
        this.id = id
        this.note = note
        this.timestamp = timestamp
    }

    companion object {
        const val TABLE_NAME = "hexoskin_data"
        const val COLUMN_ID = "id"
        const val COLUMN_HEART_RATE = "HeartRate"
        const val COLUMN_RESP_RATE = "RespRate"
        const val COLUMN_INSP = "Insp"
        const val COLUMN_EXP = "Exp"
        const val COLUMN_CADENCE = "Cadence"
        const val COLUMN_STEP_COUNT = "Steps"
        const val COLUMN_CLI = "Clitime"
        const val COLUMN_ACT = "Activity"
        const val COLUMN_MIL = "Millisec"
        const val COLUMN_TIMESTAMP = "timestamp"

        // Create table SQL query
        const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
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
                + ")")
    }
}