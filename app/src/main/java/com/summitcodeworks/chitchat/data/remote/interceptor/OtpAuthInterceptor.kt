package com.summitcodeworks.chitchat.data.remote.interceptor

import android.util.Log
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OTP Authentication Interceptor
 *
 * Automatically adds OTP access tokens to API requests for authenticated endpoints
 * following the ChitChat OTP Authentication Guide:
 * - Adds "Authorization: Bearer <access-token>" header to all requests
 * - Excludes public endpoints that don't require authentication
 * - Handles token expiration gracefully
 */
@Singleton
class OtpAuthInterceptor @Inject constructor(
    private val otpAuthManager: OtpAuthManager
) : Interceptor {

    companion object {
        private const val TAG = "OtpAuthInterceptor"
        private const val AUTHORIZATION_HEADER = "Authorization"

        // Public endpoints that don't require authentication
        private val PUBLIC_ENDPOINTS = setOf(
            "/api/users/send-otp",
            "/api/users/verify-otp",
            "/api/users/authenticate", // Firebase auth (legacy)
            "/api/users/register",
            "/api/users/login",
            "/api/users/refresh-token",
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for public endpoints
        if (isPublicEndpoint(originalRequest.url.encodedPath)) {
            Log.d(TAG, "Skipping authentication for public endpoint: ${originalRequest.url.encodedPath}")
            return chain.proceed(originalRequest)
        }

        // Get the current OTP access token
        val token = otpAuthManager.getCurrentToken()

        if (token == null) {
            Log.w(TAG, "No OTP access token available for request: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }

        // Add authorization header to the request
        val authenticatedRequest = originalRequest.newBuilder()
            .header(AUTHORIZATION_HEADER, "Bearer $token")
            .build()

        Log.d(TAG, "Adding OTP access token to request: ${authenticatedRequest.url.encodedPath}")

        // Execute the request
        val response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized responses
        return if (response.code == 401) {
            handleUnauthorizedResponse(chain, originalRequest, response)
        } else {
            response
        }
    }

    /**
     * Handles 401 Unauthorized responses
     * For OTP authentication, we simply clear the token and let the user re-authenticate
     */
    private fun handleUnauthorizedResponse(
        chain: Interceptor.Chain,
        originalRequest: Request,
        originalResponse: Response
    ): Response {
        Log.w(TAG, "Received 401 Unauthorized for ${originalRequest.url.encodedPath}, clearing OTP token")

        // Close the original response to free up connection
        originalResponse.close()

        // Clear the invalid token
        otpAuthManager.signOut()

        // Create a new 401 response since we closed the original
        return createUnauthorizedResponse(originalRequest)
    }

    /**
     * Creates a 401 Unauthorized response
     */
    private fun createUnauthorizedResponse(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized - OTP access token invalid or expired")
            .body(okhttp3.ResponseBody.create(null, ""))
            .build()
    }

    /**
     * Checks if the endpoint is public and doesn't require authentication
     */
    private fun isPublicEndpoint(path: String): Boolean {
        return PUBLIC_ENDPOINTS.any { publicPath ->
            path.startsWith(publicPath)
        }
    }
}
