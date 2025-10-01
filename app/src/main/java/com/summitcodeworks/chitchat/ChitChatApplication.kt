package com.summitcodeworks.chitchat

import android.app.Application
import com.summitcodeworks.networkmonitor.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for ChitChat Android application.
 * 
 * This class serves as the entry point for the application and handles global initialization
 * tasks that need to be performed when the app starts. It uses Hilt for dependency injection
 * and initializes the NetworkMonitor module for network debugging capabilities.
 * 
 * Key responsibilities:
 * - Initialize Hilt dependency injection framework
 * - Set up NetworkMonitor for network request monitoring and debugging
 * - Handle any global application configuration
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@HiltAndroidApp
class ChitChatApplication : Application() {

    /**
     * Called when the application is first created.
     * 
     * This method performs essential initialization tasks:
     * - Initializes the NetworkMonitor module for network debugging
     * - Sets up any global application state
     * - Handles initialization errors gracefully to prevent app crashes
     * 
     * The NetworkMonitor initialization is wrapped in a try-catch block to ensure
     * the app continues to function even if the network monitoring setup fails.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize NetworkMonitor for network request debugging and monitoring
        // This allows developers to inspect all HTTP requests, responses, and errors
        // in real-time during development and testing phases
        try {
            NetworkMonitor.initialize(this)
        } catch (e: Exception) {
            // Handle initialization errors gracefully to prevent app crashes
            // Network monitoring is a debugging feature, so its failure shouldn't
            // affect the core functionality of the application
        }
    }
}
