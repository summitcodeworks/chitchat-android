package com.summitcodeworks.chitchat.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Firebase device tokens for push notifications
 */
@Singleton
class FirebaseTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FirebaseTokenManager"
        private const val PREFS_NAME = "firebase_token_prefs"
        private const val KEY_DEVICE_TOKEN = "device_token"
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentToken = MutableStateFlow<String?>(getSavedToken())
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    /**
     * Gets the current Firebase device token
     */
    suspend fun getDeviceToken(): Result<String> {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Firebase token retrieved: ${token.take(20)}...")

            // Save token locally
            saveToken(token)
            _currentToken.value = token

            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firebase token", e)
            Result.failure(e)
        }
    }

    /**
     * Forces refresh of the Firebase device token
     */
    suspend fun refreshDeviceToken(): Result<String> {
        return try {
            // Delete current token to force refresh
            FirebaseMessaging.getInstance().deleteToken().await()

            // Get new token
            val newToken = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Firebase token refreshed: ${newToken.take(20)}...")

            // Save new token locally
            saveToken(newToken)
            _currentToken.value = newToken

            Result.success(newToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh Firebase token", e)
            Result.failure(e)
        }
    }

    /**
     * Gets the locally saved token (if any)
     */
    fun getSavedToken(): String? {
        return sharedPreferences.getString(KEY_DEVICE_TOKEN, null)
    }

    /**
     * Saves the token locally
     */
    private fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_DEVICE_TOKEN, token)
            .apply()
    }

    /**
     * Clears the saved token (useful for logout)
     */
    fun clearSavedToken() {
        sharedPreferences.edit()
            .remove(KEY_DEVICE_TOKEN)
            .apply()
        _currentToken.value = null
    }

    /**
     * Checks if we have a valid token
     */
    fun hasValidToken(): Boolean {
        return !getSavedToken().isNullOrBlank()
    }
}