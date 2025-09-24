package com.summitcodeworks.chitchat.di

import android.content.Context
import com.chuckerteam.chucker.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing ChuckerInterceptor
 * Chucker is a network inspector for Android applications
 */
@Module
@InstallIn(SingletonComponent::class)
object ChuckerModule {

    @Provides
    @Singleton
    fun provideChuckerInterceptor(@ApplicationContext context: Context): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(ChuckerInterceptor.getDefaultCollector(context))
            .maxContentLength(250_000L)
            .redactHeaders("Authorization", "Cookie")
            .alwaysReadResponseBody(false)
            .build()
    }
}
