package com.summitcodeworks.chitchat

import android.app.Application
import com.summitcodeworks.networkmonitor.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChitChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize NetworkMonitor
        try {
            NetworkMonitor.initialize(this)
        } catch (e: Exception) {
            // Handle initialization errors gracefully
        }
    }
}
