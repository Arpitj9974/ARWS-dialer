package com.arws.hrcalltracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase — Room Database instance for HR Call Tracker.
 * Manages the SQLite database that holds pending offline calls.
 */
@Database(entities = [CallEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun callDao(): CallDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hr_call_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
