package com.summitcodeworks.chitchat.data.remote.interceptor

import com.google.gson.Gson
import com.summitcodeworks.chitchat.data.remote.dto.ApiErrorResponse
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorResponseInterceptor @Inject constructor() : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // Handle error responses
        if (!response.isSuccessful) {
            val errorBody = response.body?.string()

            if (!errorBody.isNullOrEmpty()) {
                try {
                    val apiError = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                    // Log structured error for debugging
                    android.util.Log.e(
                        "API_ERROR",
                        "HTTP ${response.code}: ${apiError.message}. " +
                        "Errors: ${apiError.errors?.joinToString { "${it.field}: ${it.message}" }}"
                    )
                } catch (e: Exception) {
                    // Log raw error if JSON parsing fails
                    android.util.Log.e("API_ERROR", "HTTP ${response.code}: $errorBody")
                }
            } else {
                android.util.Log.e("API_ERROR", "HTTP ${response.code}: Empty error response")
            }

            // Return response with new body since we consumed the original
            return response.newBuilder()
                .body(okhttp3.ResponseBody.create(response.body?.contentType(), errorBody ?: ""))
                .build()
        }

        return response
    }
}