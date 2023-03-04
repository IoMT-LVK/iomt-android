package com.iomt.android

/**
 * @property id
 * @property note
 * @property timestamp
 */
@Suppress("MISSING_KDOC_CLASS_ELEMENTS")
data class Note(val id: Int, val note: Map<String, String>? = null, val timestamp: String? = null) {
    companion object {
        const val COLUMN_ACT = "Activity"
        const val COLUMN_CADENCE = "Cadence"
        const val COLUMN_CLI = "Clitime"
        const val COLUMN_EXP = "Exp"
        const val COLUMN_HEART_RATE = "HeartRate"
        const val COLUMN_ID = "id"
        const val COLUMN_INSP = "Insp"
        const val COLUMN_MIL = "Millisec"
        const val COLUMN_RESP_RATE = "RespRate"
        const val COLUMN_STEP_COUNT = "Steps"
        const val COLUMN_TIMESTAMP = "timestamp"

        const val TABLE_NAME = "hexoskin_data"

        /**
         * Get SQL create table query
         *
         * @param tableName name of a table in db, [TABLE_NAME] by default
         * @return SQL create table query
         */
        @Suppress("LONG_LINE")
        fun createTable(tableName: String = TABLE_NAME) = buildString {
            append("CREATE TABLE ")
            append(tableName)
            append("(")
            append(COLUMN_ID)
            append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
            append(COLUMN_HEART_RATE)
            append(" INTEGER,")
            append(COLUMN_RESP_RATE)
            append(" INTEGER,")
            append(COLUMN_INSP)
            append(" FLOAT,")
            append(COLUMN_EXP)
            append(" FLOAT,")
            append(COLUMN_CADENCE)
            append(" INTEGER,")
            append(COLUMN_STEP_COUNT)
            append(" INTEGER,")
            append(COLUMN_ACT)
            append(" FLOAT,")
            append(COLUMN_CLI)
            append(" TEXT,")
            append(COLUMN_MIL)
            append(" INTEGER,")
            append(COLUMN_TIMESTAMP)
            append(" DATETIME DEFAULT CURRENT_TIMESTAMP)")
        }
    }
}
