package com.summitcodeworks.networkmonitor.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.WebSocketEvent

@Database(
    entities = [NetworkLog::class, WebSocketEvent::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NetworkMonitorDatabase : RoomDatabase() {

    abstract fun networkLogDao(): NetworkLogDao
    abstract fun webSocketEventDao(): WebSocketEventDao

    companion object {
        private const val DATABASE_NAME = "network_monitor_db"

        fun create(context: Context): NetworkMonitorDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                NetworkMonitorDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // This will recreate the database if schema changes
            .build()
        }
    }
}