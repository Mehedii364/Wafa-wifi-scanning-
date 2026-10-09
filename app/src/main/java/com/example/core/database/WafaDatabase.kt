package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WifiSessionEntity::class], version = 1, exportSchema = false)
abstract class WafaDatabase : RoomDatabase() {

    abstract fun wifiSessionDao(): WifiSessionDao

    companion object {
        @Volatile
        private var INSTANCE: WafaDatabase? = null

        fun getInstance(context: Context): WafaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WafaDatabase::class.java,
                    "wafa_wifi_sense.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
