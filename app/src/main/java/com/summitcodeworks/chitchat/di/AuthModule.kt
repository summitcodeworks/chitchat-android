package com.summitcodeworks.chitchat.di

import android.content.Context
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideOtpAuthManager(
        @ApplicationContext context: Context
    ): OtpAuthManager {
        return OtpAuthManager(context)
    }
}
