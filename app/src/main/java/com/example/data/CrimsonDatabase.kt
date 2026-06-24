package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserSettings::class, DailyLog::class], version = 1, exportSchema = false)
abstract class CrimsonDatabase : RoomDatabase() {
    abstract fun crimsonDao(): CrimsonDao

    companion object {
        @Volatile
        private var INSTANCE: CrimsonDatabase? = null

        fun getDatabase(context: Context): CrimsonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrimsonDatabase::class.java,
                    "crimson_care_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
