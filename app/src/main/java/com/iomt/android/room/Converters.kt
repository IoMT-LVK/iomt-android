package com.iomt.android.room

import androidx.room.TypeConverter

import java.util.UUID

import kotlinx.datetime.LocalDateTime

/**
 * Class containing [TypeConverter]s for database fields
 */
class Converters {
    /**
     * @param uuid [UUID]
     * @return [uuid] as [String]
     */
    @TypeConverter
    fun fromUuid(uuid: UUID): String = uuid.toString()

    /**
     * @param uuidString [UUID] as [String]
     * @return [uuidString] as [UUID]
     */
    @TypeConverter
    fun toUuid(uuidString: String): UUID = UUID.fromString(uuidString)

    /**
     * @param localDateTime [LocalDateTime]
     * @return [LocalDateTime] as [String]
     */
    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime): String = localDateTime.toString()

    /**
     * @param localDateTimeString [LocalDateTime] as [String]
     * @return [localDateTimeString] as [LocalDateTime]
     */
    @TypeConverter
    fun toLocalDateTime(localDateTimeString: String): LocalDateTime = LocalDateTime.parse(localDateTimeString)
}
