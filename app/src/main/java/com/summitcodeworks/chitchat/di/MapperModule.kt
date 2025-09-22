package com.summitcodeworks.chitchat.di

import com.summitcodeworks.chitchat.data.mapper.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {
    
    @Provides
    @Singleton
    fun provideUserMapper(): UserMapper = UserMapper()
    
    @Provides
    @Singleton
    fun provideMessageMapper(): MessageMapper = MessageMapper()
    
    @Provides
    @Singleton
    fun provideCallMapper(): CallMapper = CallMapper()
    
    @Provides
    @Singleton
    fun provideGroupMapper(): GroupMapper = GroupMapper()
    
    @Provides
    @Singleton
    fun provideNotificationMapper(): NotificationMapper = NotificationMapper()
    
    @Provides
    @Singleton
    fun provideMediaMapper(): MediaMapper = MediaMapper()
    
    @Provides
    @Singleton
    fun provideStatusMapper(): com.summitcodeworks.chitchat.data.mapper.StatusMapper = com.summitcodeworks.chitchat.data.mapper.StatusMapper()
}
