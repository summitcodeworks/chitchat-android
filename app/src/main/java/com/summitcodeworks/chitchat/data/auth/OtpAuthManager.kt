package com.summitcodeworks.chitchat.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.summitcodeworks.chitchat.data.remote.dto.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OTP Authentication Manager
 * 
 * Handles SMS-based OTP authentication as the primary authentication method
 * for the ChitChat application. This replaces Firebase authentication
 * with a simpler, more direct approach.
 */
@Singleton
class OtpAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "otp_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_IN = "expires_in"
        private const val KEY_TOKEN_EXPIRY = "token_expiry_time"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PHONE_NUMBER = "phone_number"
    }

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _currentToken = MutableStateFlow<String?>(getSavedToken())
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserDto?>(getSavedUser())
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    init {
        // Check if we have a valid token on initialization
        checkAuthenticationStatus()
    }

    /**
     * Sets the authentication data after successful OTP verification
     */
    fun setAuthData(authResponse: OtpAuthResponse) {
        saveAuthData(authResponse)
        _isAuthenticated.value = true
        _currentToken.value = authResponse.accessToken
        _currentUser.value = authResponse.user
    }

    /**
     * Gets the current access token
     */
    fun getCurrentToken(): String? = _currentToken.value

    /**
     * Checks if the user is currently authenticated
     */
    fun isUserAuthenticated(): Boolean = _isAuthenticated.value

    /**
     * Gets the current user
     */
    fun getCurrentUser(): UserDto? = _currentUser.value

    /**
     * Gets the current user's ID
     */
    fun getCurrentUserId(): Long? = _currentUser.value?.id

    /**
     * Gets the current user's phone number
     */
    fun getCurrentUserPhone(): String? = _currentUser.value?.phoneNumber

    /**
     * Signs out the current user
     */
    fun signOut() {
        clearAuthData()
        _isAuthenticated.value = false
        _currentToken.value = null
        _currentUser.value = null
    }

    /**
     * Refreshes the authentication status
     */
    private fun checkAuthenticationStatus() {
        val token = getSavedToken()
        val user = getSavedUser()
        
        if (token != null && user != null && !isTokenExpired()) {
            _isAuthenticated.value = true
            _currentToken.value = token
            _currentUser.value = user
        } else {
            clearAuthData()
        }
    }

    /**
     * Saves authentication data to SharedPreferences
     */
    private fun saveAuthData(authResponse: OtpAuthResponse) {
        val currentTime = System.currentTimeMillis()
        val expiryTime = currentTime + (authResponse.expiresIn * 1000L)
        
        sharedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, authResponse.accessToken)
            .putString(KEY_TOKEN_TYPE, authResponse.tokenType)
            .putInt(KEY_EXPIRES_IN, authResponse.expiresIn)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .putLong(KEY_USER_ID, authResponse.user.id)
            .putString(KEY_PHONE_NUMBER, authResponse.user.phoneNumber)
            .apply()
    }

    /**
     * Gets the saved token from SharedPreferences
     */
    private fun getSavedToken(): String? {
        return sharedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Gets the saved user from SharedPreferences
     */
    private fun getSavedUser(): UserDto? {
        val userId = sharedPrefs.getLong(KEY_USER_ID, -1L)
        val phoneNumber = sharedPrefs.getString(KEY_PHONE_NUMBER, null)
        
        return if (userId != -1L && phoneNumber != null) {
            // Create a basic UserDto from saved data
            // In a real implementation, you might want to fetch full user data
            UserDto(
                id = userId,
                phoneNumber = phoneNumber,
                name = "User", // This would be fetched from the server
                avatarUrl = null,
                about = null,
                lastSeen = null,
                isOnline = false,
                createdAt = null
            )
        } else {
            null
        }
    }

    /**
     * Checks if the current token is expired
     */
    private fun isTokenExpired(): Boolean {
        val expiryTime = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        return System.currentTimeMillis() >= expiryTime
    }

    /**
     * Clears all authentication data
     */
    private fun clearAuthData() {
        sharedPrefs.edit().clear().apply()
    }
}
