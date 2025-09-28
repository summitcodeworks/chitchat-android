package com.summitcodeworks.chitchat.di

import android.content.Context
import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {
    
    @Provides
    @Singleton
    fun provideEnvironmentManager(
        @ApplicationContext context: Context
    ): EnvironmentManager {
        return EnvironmentManager(context)
    }
}
