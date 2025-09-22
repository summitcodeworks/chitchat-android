package com.summitcodeworks.chitchat.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.summitcodeworks.chitchat.data.local.dao.*
import com.summitcodeworks.chitchat.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        CallEntity::class,
        StatusEntity::class,
        MediaEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChitChatDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun callDao(): CallDao
    abstract fun statusDao(): StatusDao
    abstract fun mediaDao(): MediaDao
    abstract fun notificationDao(): NotificationDao
    
    companion object {
        @Volatile
        private var INSTANCE: ChitChatDatabase? = null
        
        fun getDatabase(context: Context): ChitChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChitChatDatabase::class.java,
                    "chitchat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
