package com.summitcodeworks.networkmonitor.di

import android.content.Context
import com.google.gson.Gson
import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.database.NetworkMonitorDatabase
import com.summitcodeworks.networkmonitor.database.WebSocketEventDao
import com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor
import com.summitcodeworks.networkmonitor.notification.NetworkMonitorNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkMonitorModule {

    @Provides
    @Singleton
    fun provideNetworkMonitorDatabase(@ApplicationContext context: Context): NetworkMonitorDatabase {
        return NetworkMonitorDatabase.create(context)
    }

    @Provides
    fun provideNetworkLogDao(database: NetworkMonitorDatabase): NetworkLogDao {
        return database.networkLogDao()
    }

    @Provides
    fun provideWebSocketEventDao(database: NetworkMonitorDatabase): WebSocketEventDao {
        return database.webSocketEventDao()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitorInterceptor(
        networkLogDao: NetworkLogDao,
        gson: Gson
    ): NetworkMonitorInterceptor {
        return NetworkMonitorInterceptor(networkLogDao, gson)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitorNotificationManager(
        @ApplicationContext context: Context,
        networkLogDao: NetworkLogDao,
        webSocketEventDao: WebSocketEventDao
    ): NetworkMonitorNotificationManager {
        return NetworkMonitorNotificationManager(context, networkLogDao, webSocketEventDao)
    }
}