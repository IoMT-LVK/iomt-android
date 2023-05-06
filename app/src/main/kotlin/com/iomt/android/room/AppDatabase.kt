package com.iomt.android.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iomt.android.room.characteristic.CharacteristicDao
import com.iomt.android.room.characteristic.CharacteristicEntity
import com.iomt.android.room.device.DeviceDao
import com.iomt.android.room.device.DeviceEntity
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkDao
import com.iomt.android.room.devicechar.DeviceCharacteristicLinkEntity
import com.iomt.android.room.record.RecordDao
import com.iomt.android.room.record.RecordEntity
import com.iomt.android.room.statistics.StatisticsDao
import com.iomt.android.room.statistics.StatisticsEntity

/**
 * Class that encapsulates database interactions
 */
@Database(
    entities = [
        CharacteristicEntity::class,
        DeviceEntity::class,
        DeviceCharacteristicLinkEntity::class,
        RecordEntity::class,
        StatisticsEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * @return [CharacteristicDao]
     */
    abstract fun characteristicDao(): CharacteristicDao

    /**
     * @return [DeviceDao]
     */
    abstract fun deviceDao(): DeviceDao

    /**
     * @return [DeviceCharacteristicLinkDao]
     */
    abstract fun deviceCharacteristicLinkDao(): DeviceCharacteristicLinkDao

    /**
     * @return [RecordDao]
     */
    abstract fun recordDao(): RecordDao

    /**
     * @return [StatisticsDao]
     */
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * @param context [Context]
         * @return [AppDatabase]
         */
        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                instance ?: run {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "app_database",
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
                return instance!!
            }
        }
    }
}
