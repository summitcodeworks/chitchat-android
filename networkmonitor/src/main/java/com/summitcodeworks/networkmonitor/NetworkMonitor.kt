package com.summitcodeworks.networkmonitor

import android.content.Context
import android.content.Intent
import com.summitcodeworks.networkmonitor.database.NetworkMonitorDatabase
import com.summitcodeworks.networkmonitor.interceptor.NetworkMonitorInterceptor
import com.summitcodeworks.networkmonitor.notification.NetworkMonitorNotificationManager
import com.summitcodeworks.networkmonitor.ui.NetworkMonitorActivity
import com.summitcodeworks.networkmonitor.websocket.NetworkMonitorWebSocketListener
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    private val context: Context,
    private val interceptor: NetworkMonitorInterceptor,
    private val notificationManager: NetworkMonitorNotificationManager,
    private val database: NetworkMonitorDatabase
) {

    companion object {
        private var instance: NetworkMonitor? = null

        fun initialize(context: Context): NetworkMonitor {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val database = NetworkMonitorDatabase.create(context)
                        val notificationManager = NetworkMonitorNotificationManager(
                            context,
                            database.networkLogDao(),
                            database.webSocketEventDao()
                        )
                        val interceptor = NetworkMonitorInterceptor(
                            database.networkLogDao(),
                            com.google.gson.Gson()
                        )
                        instance = NetworkMonitor(context, interceptor, notificationManager, database)
                    }
                }
            }
            return instance!!
        }

        fun getInstance(): NetworkMonitor? = instance
    }

    /**
     * Get the HTTP interceptor for OkHttp
     */
    fun getInterceptor(): NetworkMonitorInterceptor = interceptor

    /**
     * Create a WebSocket listener that monitors WebSocket events
     */
    fun createWebSocketListener(originalListener: WebSocketListener? = null): NetworkMonitorWebSocketListener {
        return NetworkMonitorWebSocketListener(
            database.networkLogDao(),
            database.webSocketEventDao(),
            originalListener
        )
    }

    /**
     * Show the notification with network activity summary
     */
    fun showNotification() {
        // Notification is automatically shown when NetworkMonitorNotificationManager is initialized
    }

    /**
     * Hide the notification
     */
    fun hideNotification() {
        notificationManager.hideNotification()
    }

    /**
     * Launch the NetworkMonitor UI
     */
    fun launchUI() {
        val intent = Intent(context, NetworkMonitorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Clear all network logs
     */
    suspend fun clearLogs() {
        database.networkLogDao().clearAllLogs()
        database.webSocketEventDao().clearAllEvents()
    }

    /**
     * Get network logs count
     */
    suspend fun getLogsCount(): Int {
        return database.networkLogDao().getLogCount() + database.webSocketEventDao().getEventCount()
    }

    /**
     * Enable/disable network monitoring
     */
    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            showNotification()
        } else {
            hideNotification()
        }
    }

    /**
     * Check if monitoring is enabled
     */
    fun isEnabled(): Boolean {
        // Check if notification is shown or monitoring is active
        return true // Simplified - in real implementation, track this state
    }
}