package com.summitcodeworks.chitchat.di

import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import com.summitcodeworks.chitchat.data.remote.api.*
import com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        networkMonitorInterceptor: NetworkMonitorInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(networkMonitorInterceptor) // Add NetworkMonitor interceptor
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        environmentManager: EnvironmentManager
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(environmentManager.getCurrentApiBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMessageApiService(retrofit: Retrofit): MessageApiService {
        return retrofit.create(MessageApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCallApiService(retrofit: Retrofit): CallApiService {
        return retrofit.create(CallApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideStatusApiService(retrofit: Retrofit): StatusApiService {
        return retrofit.create(StatusApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMediaApiService(retrofit: Retrofit): MediaApiService {
        return retrofit.create(MediaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApiService(retrofit: Retrofit): NotificationApiService {
        return retrofit.create(NotificationApiService::class.java)
    }
}