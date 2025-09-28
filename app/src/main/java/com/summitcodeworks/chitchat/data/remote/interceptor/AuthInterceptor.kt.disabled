package com.summitcodeworks.chitchat.data.remote.interceptor

import com.summitcodeworks.chitchat.data.auth.AuthTokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val authTokenManager: AuthTokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for auth endpoints
        val url = originalRequest.url.toString()
        if (url.contains("/api/users/authenticate") ||
            url.contains("/api/admin/login") ||
            url.contains("/api/auth/")) {
            return chain.proceed(originalRequest)
        }

        // Add auth token if available
        val requestBuilder = originalRequest.newBuilder()
        val token = authTokenManager.getCurrentToken()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        // Add common headers
        requestBuilder
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "ChitChat-Android/1.0")

        return chain.proceed(requestBuilder.build())
    }

    fun setAuthToken(token: String?) {
        if (token != null) {
            authTokenManager.setToken(token)
        } else {
            authTokenManager.clearToken()
        }
    }

    fun clearAuthToken() {
        authTokenManager.clearToken()
    }

    fun getAuthToken(): String? = authTokenManager.getCurrentToken()
}