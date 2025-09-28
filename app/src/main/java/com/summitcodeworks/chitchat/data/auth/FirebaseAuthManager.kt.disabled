package com.summitcodeworks.chitchat.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * FirebaseAuthManager following ChitChat Authentication Guide
 *
 * Handles Firebase ID token management, automatic refresh, and token validation
 * as per the API Gateway requirements documented at:
 * - Production: http://65.1.185.194:9101
 * - Uses Firebase ID tokens as primary authentication method
 */
@Singleton
class FirebaseAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "FirebaseAuthManager"
        private const val PREFS_NAME = "firebase_auth_prefs"
        private const val KEY_CACHED_TOKEN = "cached_firebase_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry_time"
        private const val TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000L // 5 minutes in milliseconds
        private const val TOKEN_LIFETIME = 60 * 60 * 1000L // 1 hour as per Firebase docs
    }

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentToken = MutableStateFlow<String?>(getCachedToken())
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        setupAuthStateListener()
        setupAutoRefresh()
    }

    /**
     * Sets up Firebase auth state listener to handle authentication changes
     */
    private fun setupAuthStateListener() {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            Log.d(TAG, "Auth state changed: user=${user?.uid}")

            if (user != null) {
                _isAuthenticated.value = true
                // Get fresh token when user authenticates
                refreshTokenAsync()
            } else {
                _isAuthenticated.value = false
                clearToken()
            }
        }
    }

    /**
     * Sets up automatic token refresh every 50 minutes as per best practices
     */
    private fun setupAutoRefresh() {
        // This would typically be handled by a background service or WorkManager
        // For now, we refresh on-demand when getValidToken() is called
    }

    /**
     * Gets a valid Firebase ID token, refreshing if necessary
     * This is the primary method for getting tokens to use with the API
     *
     * @param forceRefresh Force refresh the token even if it appears valid
     * @return Valid Firebase ID token or null if user not authenticated
     */
    suspend fun getValidToken(forceRefresh: Boolean = false): String? {
        val currentUser = firebaseAuth.currentUser ?: return null

        return try {
            // Check if we need to refresh the token
            val shouldRefresh = forceRefresh || isTokenExpiringSoon()

            Log.d(TAG, "Getting token: forceRefresh=$forceRefresh, shouldRefresh=$shouldRefresh")

            val result = currentUser.getIdToken(shouldRefresh).await()
            val token = result.token

            if (token != null) {
                cacheToken(token)
                _currentToken.value = token
                Log.d(TAG, "Token obtained successfully")
            } else {
                Log.w(TAG, "Token is null after Firebase getIdToken call")
            }

            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get Firebase ID token", e)
            null
        }
    }

    /**
     * Gets the current cached token without refresh
     * Use this for quick access when you're sure the token is valid
     */
    fun getCurrentToken(): String? = _currentToken.value

    /**
     * Checks if the current user is authenticated with Firebase
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Gets the current Firebase user
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Gets the current user's UID
     */
    fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Gets the current user's phone number
     */
    fun getCurrentUserPhone(): String? {
        return firebaseAuth.currentUser?.phoneNumber
    }

    /**
     * Gets the current user's email
     */
    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    /**
     * Gets the current user's display name
     */
    fun getCurrentUserName(): String? {
        return firebaseAuth.currentUser?.displayName
    }

    /**
     * Observes Firebase authentication state changes
     */
    fun observeAuthState() = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }.distinctUntilChanged()

    /**
     * Forces a token refresh
     * Useful when you encounter 401 errors and need a fresh token
     */
    suspend fun refreshToken(): String? {
        Log.d(TAG, "Forcing token refresh due to 401 error")
        val token = getValidToken(forceRefresh = true)
        if (token != null) {
            Log.d(TAG, "Token refresh successful, new token obtained")
        } else {
            Log.e(TAG, "Token refresh failed - no token returned")
        }
        return token
    }

    /**
     * Signs out the current user and clears all cached tokens
     */
    suspend fun signOut() {
        Log.d(TAG, "Signing out user")
        clearToken()
        firebaseAuth.signOut()
    }

    /**
     * Debug method to get a fresh token and log it for testing
     * This should only be used for debugging purposes
     */
    suspend fun getFreshTokenForTesting(): String? {
        val token = getValidToken(forceRefresh = true)
        if (token != null) {
            Log.d(TAG, "=== FRESH TOKEN FOR TESTING ===")
            Log.d(TAG, "Token: $token")
            Log.d(TAG, "=== END TOKEN ===")
        } else {
            Log.e(TAG, "Failed to get fresh token for testing")
        }
        return token
    }

    /**
     * Clears cached token and updates state
     */
    private fun clearToken() {
        Log.d(TAG, "Clearing cached token")
        _currentToken.value = null
        sharedPrefs.edit()
            .remove(KEY_CACHED_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }

    /**
     * Caches the token with expiry time
     */
    private fun cacheToken(token: String) {
        val expiryTime = System.currentTimeMillis() + TOKEN_LIFETIME
        sharedPrefs.edit()
            .putString(KEY_CACHED_TOKEN, token)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()

        Log.d(TAG, "Token cached with expiry: ${java.text.SimpleDateFormat.getInstance().format(java.util.Date(expiryTime))}")
    }

    /**
     * Gets cached token if not expired
     */
    private fun getCachedToken(): String? {
        val token = sharedPrefs.getString(KEY_CACHED_TOKEN, null)
        val expiryTime = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)

        return if (token != null && System.currentTimeMillis() < expiryTime - TOKEN_REFRESH_THRESHOLD) {
            Log.d(TAG, "Using cached token")
            token
        } else {
            if (token != null) {
                Log.d(TAG, "Cached token expired or expiring soon")
            }
            null
        }
    }

    /**
     * Checks if the cached token is expiring soon
     */
    private fun isTokenExpiringSoon(): Boolean {
        val expiryTime = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        val isExpiringSoon = System.currentTimeMillis() > (expiryTime - TOKEN_REFRESH_THRESHOLD)

        if (isExpiringSoon) {
            Log.d(TAG, "Token is expiring soon")
        }

        return isExpiringSoon
    }

    /**
     * Refreshes token asynchronously without blocking
     */
    private fun refreshTokenAsync() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            currentUser.getIdToken(true)
                .addOnSuccessListener { result ->
                    val token = result.token
                    if (token != null) {
                        cacheToken(token)
                        _currentToken.value = token
                        Log.d(TAG, "Token refreshed asynchronously")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to refresh token asynchronously", e)
                }
        }
    }

    /**
     * Creates authorization header value for API requests
     * Format: "Bearer <firebase-id-token>"
     */
    suspend fun getAuthorizationHeader(): String? {
        val token = getValidToken()
        return if (token != null) "Bearer $token" else null
    }

    /**
     * Validates if the current token is valid
     * This is a local check - actual validation happens on the server
     */
    fun isTokenValid(): Boolean {
        val token = getCurrentToken()
        val expiryTime = sharedPrefs.getLong(KEY_TOKEN_EXPIRY, 0L)

        return token != null &&
               System.currentTimeMillis() < expiryTime &&
               isUserAuthenticated()
    }
}