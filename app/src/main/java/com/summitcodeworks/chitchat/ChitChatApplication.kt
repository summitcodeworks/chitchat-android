package com.summitcodeworks.chitchat

import android.app.Application
import com.summitcodeworks.networkmonitor.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChitChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize NetworkMonitor
        if (BuildConfig.DEBUG) {
            NetworkMonitor.initialize(this)
        }
    }
}
