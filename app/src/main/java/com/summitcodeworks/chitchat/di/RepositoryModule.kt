package com.summitcodeworks.chitchat.di

import com.summitcodeworks.chitchat.data.repository.OtpAuthRepository
import com.summitcodeworks.chitchat.data.repository.MessageRepository
import com.summitcodeworks.chitchat.data.repository.CallRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideOtpAuthRepository(
        userApiService: com.summitcodeworks.chitchat.data.remote.api.UserApiService,
        userDao: com.summitcodeworks.chitchat.data.local.dao.UserDao,
        userMapper: com.summitcodeworks.chitchat.data.mapper.UserMapper,
        otpAuthManager: com.summitcodeworks.chitchat.data.auth.OtpAuthManager
    ): OtpAuthRepository {
        return OtpAuthRepository(userApiService, userDao, otpAuthManager, userMapper)
    }
    
    @Provides
    @Singleton
    fun provideMessageRepository(
        messageApiService: com.summitcodeworks.chitchat.data.remote.api.MessageApiService,
        messageDao: com.summitcodeworks.chitchat.data.local.dao.MessageDao,
        messageMapper: com.summitcodeworks.chitchat.data.mapper.MessageMapper,
        webSocketClient: com.summitcodeworks.chitchat.data.remote.websocket.ChitChatWebSocketClient
    ): MessageRepository {
        return MessageRepository(messageApiService, messageDao, messageMapper, webSocketClient)
    }
    
    @Provides
    @Singleton
    fun provideCallRepository(
        callApiService: com.summitcodeworks.chitchat.data.remote.api.CallApiService,
        callDao: com.summitcodeworks.chitchat.data.local.dao.CallDao,
        callMapper: com.summitcodeworks.chitchat.data.mapper.CallMapper
    ): CallRepository {
        return CallRepository(callApiService, callDao, callMapper)
    }
    
    @Provides
    @Singleton
    fun provideGroupRepository(
        groupApiService: com.summitcodeworks.chitchat.data.remote.api.GroupApiService,
        groupDao: com.summitcodeworks.chitchat.data.local.dao.GroupDao,
        groupMemberDao: com.summitcodeworks.chitchat.data.local.dao.GroupMemberDao,
        groupMapper: com.summitcodeworks.chitchat.data.mapper.GroupMapper
    ): com.summitcodeworks.chitchat.data.repository.GroupRepository {
        return com.summitcodeworks.chitchat.data.repository.GroupRepositoryImpl(
            groupApiService, groupDao, groupMemberDao, groupMapper
        )
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationApiService: com.summitcodeworks.chitchat.data.remote.api.NotificationApiService,
        notificationDao: com.summitcodeworks.chitchat.data.local.dao.NotificationDao,
        notificationMapper: com.summitcodeworks.chitchat.data.mapper.NotificationMapper
    ): com.summitcodeworks.chitchat.data.repository.NotificationRepository {
        return com.summitcodeworks.chitchat.data.repository.NotificationRepositoryImpl(
            notificationApiService, notificationDao, notificationMapper
        )
    }
    
    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaApiService: com.summitcodeworks.chitchat.data.remote.api.MediaApiService,
        mediaDao: com.summitcodeworks.chitchat.data.local.dao.MediaDao,
        mediaMapper: com.summitcodeworks.chitchat.data.mapper.MediaMapper
    ): com.summitcodeworks.chitchat.data.repository.MediaRepository {
        return com.summitcodeworks.chitchat.data.repository.MediaRepositoryImpl(
            mediaApiService, mediaDao, mediaMapper
        )
    }
    
    @Provides
    @Singleton
    fun provideAdminRepository(
        adminApiService: com.summitcodeworks.chitchat.data.remote.api.AdminApiService,
        errorHandler: com.summitcodeworks.chitchat.data.remote.error.NetworkErrorHandler
    ): com.summitcodeworks.chitchat.data.repository.AdminRepository {
        return com.summitcodeworks.chitchat.data.repository.AdminRepositoryImpl(
            adminApiService, errorHandler
        )
    }
    
    @Provides
    @Singleton
    fun provideStatusRepository(
        statusApiService: com.summitcodeworks.chitchat.data.remote.api.StatusApiService,
        statusDao: com.summitcodeworks.chitchat.data.local.dao.StatusDao,
        statusMapper: com.summitcodeworks.chitchat.data.mapper.StatusMapper
    ): com.summitcodeworks.chitchat.data.repository.StatusRepository {
        return com.summitcodeworks.chitchat.data.repository.StatusRepositoryImpl(
            statusApiService, statusDao, statusMapper
        )
    }
}
