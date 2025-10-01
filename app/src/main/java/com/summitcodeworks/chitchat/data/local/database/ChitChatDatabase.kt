package com.summitcodeworks.chitchat.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.summitcodeworks.chitchat.data.local.dao.*
import com.summitcodeworks.chitchat.data.local.entity.*

/**
 * Main Room database for the ChitChat application.
 * 
 * This database serves as the local storage layer for all app data, providing
 * offline access and data persistence. It uses Room database framework with
 * proper entity definitions and type converters for complex data types.
 * 
 * Database entities:
 * - UserEntity: User profile information and contact details
 * - MessageEntity: Chat messages with metadata and status
 * - GroupEntity: Group chat information and settings
 * - GroupMemberEntity: Group membership and permissions
 * - CallEntity: Voice/video call logs and metadata
 * - StatusEntity: User status updates and stories
 * - MediaEntity: Media files and attachments
 * - NotificationEntity: Push notifications and alerts
 * 
 * Key features:
 * - Type-safe database operations with Room
 * - Automatic data type conversion for complex objects
 * - Database versioning and migration support
 * - Singleton pattern for efficient resource usage
 * - Destructive migration fallback for development
 * 
 * The database uses type converters to handle:
 * - Date/time objects
 * - Enum types
 * - Complex data structures
 * - JSON serialization for nested objects
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
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
    version = 2,
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
        
        /**
         * Gets the singleton instance of the ChitChat database.
         * 
         * This method implements the singleton pattern to ensure only one database
         * instance exists throughout the application lifecycle. It uses double-checked
         * locking for thread safety.
         * 
         * @param context Application context for database initialization
         * @return Singleton instance of ChitChatDatabase
         */
        fun getDatabase(context: Context): ChitChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChitChatDatabase::class.java,
                    "chitchat_database"
                )
                .fallbackToDestructiveMigration() // For development - removes data on schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
