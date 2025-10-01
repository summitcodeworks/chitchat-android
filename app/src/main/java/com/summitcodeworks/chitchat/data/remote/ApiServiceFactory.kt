package com.summitcodeworks.chitchat.data.remote

import android.content.Context
// import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import com.summitcodeworks.chitchat.data.remote.api.*
import com.summitcodeworks.chitchat.data.remote.interceptor.OtpAuthInterceptor
import com.summitcodeworks.chitchat.data.remote.interceptor.ErrorResponseInterceptor
import com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory class for creating and managing API service instances in ChitChat.
 * 
 * This factory handles the creation of Retrofit instances and API services
 * with proper configuration, interceptors, and environment management.
 * It ensures consistent network configuration across all API calls.
 * 
 * Key responsibilities:
 * - Create and configure Retrofit instances
 * - Manage environment-specific API endpoints
 * - Apply interceptors for authentication and monitoring
 * - Handle environment changes and service invalidation
 * - Provide singleton instances of API services
 * 
 * Network configuration:
 * - OTP authentication interceptor for token injection
 * - Error response interceptor for unified error handling
 * - Network monitoring interceptor for debugging
 * - HTTP logging interceptor for development
 * - Connection timeout and retry configuration
 * 
 * Environment management:
 * - Dynamic base URL switching for different environments
 * - Automatic service recreation on environment changes
 * - Support for development, staging, and production APIs
 * - Environment-specific configuration handling
 * 
 * API services provided:
 * - UserApiService: User management and profiles
 * - MessageApiService: Messaging functionality
 * - CallApiService: Voice/video calls
 * - StatusApiService: Status updates and stories
 * - MediaApiService: File uploads and media
 * - NotificationApiService: Push notifications
 * - GroupApiService: Group chat management
 * - AdminApiService: Administrative functions
 * 
 * @param environmentManager Manager for environment configuration
 * @param networkMonitorInterceptor Interceptor for network monitoring
 * @param otpAuthInterceptor Interceptor for OTP authentication
 * @param errorResponseInterceptor Interceptor for error handling
 * @param context Application context for various operations
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@Singleton
class ApiServiceFactory @Inject constructor(
    private val environmentManager: EnvironmentManager,
    private val networkMonitorInterceptor: NetworkMonitorInterceptor,
    private val otpAuthInterceptor: OtpAuthInterceptor,
    private val errorResponseInterceptor: ErrorResponseInterceptor,
    @ApplicationContext private val context: Context
) {
    private var currentBaseUrl: String? = null
    private var retrofit: Retrofit? = null

    init {
        // Register for environment change notifications
        environmentManager.addEnvironmentChangeCallback {
            invalidate()
        }
    }

    private fun getRetrofit(): Retrofit {
        val currentUrl = environmentManager.getCurrentApiBaseUrl()

        // Create new retrofit instance if base URL changed or doesn't exist
        if (retrofit == null || currentBaseUrl != currentUrl) {
            currentBaseUrl = currentUrl

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(otpAuthInterceptor)
                .addInterceptor(errorResponseInterceptor)
                .addInterceptor(networkMonitorInterceptor)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(currentUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!
    }

    fun getUserApiService(): UserApiService = getRetrofit().create(UserApiService::class.java)
    fun getMessageApiService(): MessageApiService = getRetrofit().create(MessageApiService::class.java)
    fun getCallApiService(): CallApiService = getRetrofit().create(CallApiService::class.java)
    fun getStatusApiService(): StatusApiService = getRetrofit().create(StatusApiService::class.java)
    fun getMediaApiService(): MediaApiService = getRetrofit().create(MediaApiService::class.java)
    fun getNotificationApiService(): NotificationApiService = getRetrofit().create(NotificationApiService::class.java)
    fun getGroupApiService(): GroupApiService = getRetrofit().create(GroupApiService::class.java)
    fun getAdminApiService(): AdminApiService = getRetrofit().create(AdminApiService::class.java)

    fun invalidate() {
        retrofit = null
        currentBaseUrl = null
    }

    // These methods are now handled automatically by OtpAuthInterceptor
    // but kept for backward compatibility if needed elsewhere
    @Deprecated("Token management is now handled automatically by OtpAuthInterceptor")
    fun setAuthToken(token: String?) {
        // No-op: OTP tokens are managed automatically
    }

    @Deprecated("Token management is now handled automatically by OtpAuthInterceptor")
    fun clearAuthToken() {
        // No-op: OTP tokens are managed automatically
    }

    @Deprecated("Use OtpAuthManager.getCurrentToken() instead")
    fun getAuthToken(): String? = null
}