package com.iomt.android.room.basic

import androidx.room.*

/**
 * Data Access Object of [BasicEntity]
 */
@Dao
interface BasicDao <E : BasicEntity> {
    /**
     * @param entity [BasicEntity] to insert
     * @return [entity]s generated ID
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: E): Long

    /**
     * @param entity [BasicEntity] to update (not null id is required)
     */
    @Update
    suspend fun update(entity: E)

    /**
     * @param entity [BasicEntity] to delete (not null id is required)
     */
    @Delete
    suspend fun delete(entity: E)
}
