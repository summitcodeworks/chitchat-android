package com.summitcodeworks.chitchat.di

import com.summitcodeworks.chitchat.data.remote.ApiServiceFactory
import com.summitcodeworks.chitchat.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideUserApiService(factory: ApiServiceFactory): UserApiService {
        return factory.getUserApiService()
    }

    @Provides
    fun provideMessageApiService(factory: ApiServiceFactory): MessageApiService {
        return factory.getMessageApiService()
    }

    @Provides
    fun provideCallApiService(factory: ApiServiceFactory): CallApiService {
        return factory.getCallApiService()
    }

    @Provides
    fun provideStatusApiService(factory: ApiServiceFactory): StatusApiService {
        return factory.getStatusApiService()
    }

    @Provides
    fun provideMediaApiService(factory: ApiServiceFactory): MediaApiService {
        return factory.getMediaApiService()
    }

    @Provides
    fun provideNotificationApiService(factory: ApiServiceFactory): NotificationApiService {
        return factory.getNotificationApiService()
    }

    @Provides
    fun provideGroupApiService(factory: ApiServiceFactory): GroupApiService {
        return factory.getGroupApiService()
    }

    @Provides
    fun provideAdminApiService(factory: ApiServiceFactory): AdminApiService {
        return factory.getAdminApiService()
    }
}