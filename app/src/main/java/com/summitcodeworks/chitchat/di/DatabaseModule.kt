package com.summitcodeworks.chitchat.di

import android.content.Context
import androidx.room.Room
import com.summitcodeworks.chitchat.data.local.dao.*
import com.summitcodeworks.chitchat.data.local.database.ChitChatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideChitChatDatabase(
        @ApplicationContext context: Context
    ): ChitChatDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ChitChatDatabase::class.java,
            "chitchat_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: ChitChatDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideMessageDao(database: ChitChatDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    fun provideGroupDao(database: ChitChatDatabase): GroupDao {
        return database.groupDao()
    }
    
    @Provides
    fun provideGroupMemberDao(database: ChitChatDatabase): GroupMemberDao {
        return database.groupMemberDao()
    }
    
    @Provides
    fun provideCallDao(database: ChitChatDatabase): CallDao {
        return database.callDao()
    }
    
    @Provides
    fun provideStatusDao(database: ChitChatDatabase): StatusDao {
        return database.statusDao()
    }
    
    @Provides
    fun provideMediaDao(database: ChitChatDatabase): MediaDao {
        return database.mediaDao()
    }
    
    @Provides
    fun provideNotificationDao(database: ChitChatDatabase): NotificationDao {
        return database.notificationDao()
    }
}
