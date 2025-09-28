package com.summitcodeworks.chitchat.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.summitcodeworks.chitchat.data.remote.ApiServiceFactory
import com.summitcodeworks.chitchat.data.remote.api.*
import com.summitcodeworks.chitchat.data.remote.interceptor.ErrorResponseInterceptor
import com.summitcodeworks.chitchat.data.remote.interceptor.OtpAuthInterceptor
import com.summitcodeworks.chitchat.data.remote.websocket.ChitChatWebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOtpAuthInterceptor(
        otpAuthManager: com.summitcodeworks.chitchat.data.auth.OtpAuthManager
    ): OtpAuthInterceptor {
        return OtpAuthInterceptor(otpAuthManager)
    }

    @Provides
    @Singleton
    fun provideErrorResponseInterceptor(): ErrorResponseInterceptor {
        return ErrorResponseInterceptor()
    }

    @Provides
    @Singleton
    fun provideApiServiceFactory(
        environmentManager: com.summitcodeworks.chitchat.data.config.EnvironmentManager,
        networkMonitorInterceptor: com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor,
        otpAuthInterceptor: OtpAuthInterceptor,
        errorResponseInterceptor: ErrorResponseInterceptor,
        @ApplicationContext context: android.content.Context
    ): ApiServiceFactory {
        return ApiServiceFactory(
            environmentManager,
            networkMonitorInterceptor,
            otpAuthInterceptor,
            errorResponseInterceptor,
            context
        )
    }

    @Provides
    @Singleton
    fun provideChitChatWebSocketClient(
        gson: Gson,
        environmentManager: com.summitcodeworks.chitchat.data.config.EnvironmentManager,
        otpAuthManager: com.summitcodeworks.chitchat.data.auth.OtpAuthManager
    ): ChitChatWebSocketClient {
        return ChitChatWebSocketClient(gson, environmentManager, otpAuthManager)
    }

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