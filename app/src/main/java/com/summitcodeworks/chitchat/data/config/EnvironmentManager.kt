package com.summitcodeworks.chitchat.data.config

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "environment_prefs"
        private const val KEY_ENVIRONMENT = "selected_environment"
    }

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val environmentChangeCallbacks = mutableListOf<() -> Unit>()

    private val _currentEnvironment = MutableStateFlow(getSavedEnvironment())
    val currentEnvironment: StateFlow<Environment> = _currentEnvironment.asStateFlow()

    init {
        // Load saved environment on startup
        _currentEnvironment.value = getSavedEnvironment()
    }

    private fun getSavedEnvironment(): Environment {
        val savedName = sharedPrefs.getString(KEY_ENVIRONMENT, Environment.PRODUCTION.displayName)
        return Environment.fromDisplayName(savedName ?: Environment.PRODUCTION.displayName)
    }

    fun setEnvironment(environment: Environment) {
        _currentEnvironment.value = environment
        sharedPrefs.edit()
            .putString(KEY_ENVIRONMENT, environment.displayName)
            .apply()

        // Notify all callbacks about environment change
        environmentChangeCallbacks.forEach { it() }
    }

    fun addEnvironmentChangeCallback(callback: () -> Unit) {
        environmentChangeCallbacks.add(callback)
    }

    fun removeEnvironmentChangeCallback(callback: () -> Unit) {
        environmentChangeCallbacks.remove(callback)
    }

    fun getCurrentApiBaseUrl(): String = _currentEnvironment.value.apiBaseUrl
    fun getCurrentWebSocketBaseUrl(): String = _currentEnvironment.value.webSocketBaseUrl

    fun getAllEnvironments(): List<Environment> = Environment.values().toList()
}