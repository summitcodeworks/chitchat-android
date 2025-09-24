package com.summitcodeworks.chitchat.data.remote

import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import com.summitcodeworks.chitchat.data.remote.api.*
import com.summitcodeworks.chitchat.data.remote.interceptor.FirebaseAuthInterceptor
import com.summitcodeworks.chitchat.data.remote.interceptor.ErrorResponseInterceptor
import com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiServiceFactory @Inject constructor(
    private val environmentManager: EnvironmentManager,
    private val networkMonitorInterceptor: NetworkMonitorInterceptor,
    private val firebaseAuthInterceptor: FirebaseAuthInterceptor,
    private val errorResponseInterceptor: ErrorResponseInterceptor
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
                .addInterceptor(firebaseAuthInterceptor)
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

    // These methods are now handled automatically by FirebaseAuthInterceptor
    // but kept for backward compatibility if needed elsewhere
    @Deprecated("Token management is now handled automatically by FirebaseAuthInterceptor")
    fun setAuthToken(token: String?) {
        // No-op: Firebase tokens are managed automatically
    }

    @Deprecated("Token management is now handled automatically by FirebaseAuthInterceptor")
    fun clearAuthToken() {
        // No-op: Firebase tokens are managed automatically
    }

    @Deprecated("Use FirebaseAuthManager.getCurrentToken() instead")
    fun getAuthToken(): String? = null
}