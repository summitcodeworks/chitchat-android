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

/**
 * Hilt dependency injection module for network-related components in ChitChat.
 * 
 * This module provides all network-related dependencies including HTTP clients,
 * API services, WebSocket clients, and network interceptors. It configures
 * the complete networking stack for the application.
 * 
 * Components provided:
 * - Gson serializer for JSON handling
 * - HTTP interceptors for authentication and error handling
 * - API service factory for creating service instances
 * - WebSocket clients for real-time communication
 * - Individual API service interfaces
 * 
 * Network stack configuration:
 * - OTP authentication interceptor for automatic token injection
 * - Error response interceptor for unified error handling
 * - Network monitoring interceptor for debugging
 * - Environment-aware service factory
 * - WebSocket connection management
 * 
 * API services provided:
 * - UserApiService: User profile and management operations
 * - MessageApiService: Message sending and retrieval
 * - CallApiService: Voice/video call functionality
 * - StatusApiService: Status updates and stories
 * - MediaApiService: File upload and media handling
 * - NotificationApiService: Push notification management
 * - GroupApiService: Group chat operations
 * - AdminApiService: Administrative functions
 * 
 * All services are configured with proper interceptors for:
 * - Authentication token injection
 * - Request/response logging
 * - Error handling and retry logic
 * - Network monitoring and debugging
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
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