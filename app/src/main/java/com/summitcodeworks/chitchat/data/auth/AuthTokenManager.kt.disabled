package com.summitcodeworks.chitchat.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val PREFS_NAME = "auth_token_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _currentToken = MutableStateFlow<String?>(getSavedToken())
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    init {
        // Listen to Firebase auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                // User is signed in, get the ID token
                user.getIdToken(false).addOnSuccessListener { result ->
                    val token = result.token
                    if (token != null) {
                        setToken(token)
                    }
                }
            } else {
                // User is signed out
                clearToken()
            }
        }
    }

    private fun getSavedToken(): String? {
        return sharedPrefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setToken(token: String) {
        _currentToken.value = token
        sharedPrefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    fun clearToken() {
        _currentToken.value = null
        sharedPrefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }

    fun getCurrentToken(): String? = _currentToken.value

    suspend fun getValidToken(): String? {
        val currentUser = firebaseAuth.currentUser
        return if (currentUser != null) {
            try {
                val result = currentUser.getIdToken(true).await()
                val token = result.token
                if (token != null) {
                    setToken(token) // Update stored token
                }
                token
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}