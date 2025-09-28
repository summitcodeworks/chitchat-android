package com.summitcodeworks.networkmonitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.summitcodeworks.networkmonitor.R
import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.database.WebSocketEventDao
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkSummary
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.ui.NetworkMonitorActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Network Monitor Notification Service
 * 
 * Provides persistent notification for network monitoring similar to Chucker.
 * Shows network activity summary and allows quick access to network logs.
 */
@AndroidEntryPoint
class NetworkMonitorNotificationService : Service() {

    @Inject
    lateinit var networkLogDao: NetworkLogDao
    
    @Inject
    lateinit var webSocketEventDao: WebSocketEventDao

    private val scope = CoroutineScope(Dispatchers.IO)
    private val notificationManager = NotificationManagerCompat.from(this)
    private val requestCounter = AtomicInteger(0)
    private val wsEventCounter = AtomicInteger(0)

    companion object {
        private const val CHANNEL_ID = "network_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE = 100
        
        const val ACTION_START_MONITORING = "start_monitoring"
        const val ACTION_STOP_MONITORING = "stop_monitoring"
        const val ACTION_CLEAR_LOGS = "clear_logs"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                startForegroundService()
                startMonitoring()
            }
            ACTION_STOP_MONITORING -> {
                stopForegroundService()
            }
            ACTION_CLEAR_LOGS -> {
                clearLogs()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows network activity summary"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = createPersistentNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    private fun startMonitoring() {
        scope.launch {
            combine(
                networkLogDao.getAllLogs(),
                webSocketEventDao.getAllEvents()
            ) { logs, events ->
                Pair(logs, events)
            }.collect { (logs, events) ->
                updateNotification(logs, events)
            }
        }
    }

    private fun updateNotification(logs: List<NetworkLog>, events: List<com.summitcodeworks.networkmonitor.model.WebSocketEvent>) {
        val httpRequests = logs.count { it.type == NetworkType.HTTP }
        val webSocketEvents = events.size
        val successfulRequests = logs.count { it.responseCode != null && it.responseCode in 200..299 }
        val failedRequests = logs.count { it.responseCode != null && it.responseCode !in 200..299 && it.responseCode > 0 }
        
        val intent = createLaunchIntent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create action buttons
        val clearIntent = Intent(this, NetworkMonitorNotificationService::class.java).apply {
            action = ACTION_CLEAR_LOGS
        }
        val clearPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE + 1,
            clearIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NetworkMonitorNotificationService::class.java).apply {
            action = ACTION_STOP_MONITORING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE + 2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_network_monitor_white)
            .setContentTitle("Network Monitor")
            .setContentText("HTTP: $httpRequests | WS: $webSocketEvents | ✓: $successfulRequests | ✗: $failedRequests")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Network Activity Summary:\n" +
                "• HTTP Requests: $httpRequests\n" +
                "• WebSocket Events: $webSocketEvents\n" +
                "• Successful: $successfulRequests\n" +
                "• Failed: $failedRequests\n" +
                "• Total Data: ${formatBytes(logs.sumOf { it.requestSize + it.responseSize })}"
            ))
            .addAction(
                R.drawable.ic_clear,
                "Clear",
                clearPendingIntent
            )
            .addAction(
                R.drawable.ic_stop,
                "Stop",
                stopPendingIntent
            )
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    private fun createPersistentNotification(): android.app.Notification {
        val intent = createLaunchIntent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_network_monitor_white)
            .setContentTitle("Network Monitor")
            .setContentText("Monitoring network activity...")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    private fun createLaunchIntent(): Intent {
        return Intent(this, NetworkMonitorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private fun clearLogs() {
        scope.launch {
            networkLogDao.clearAllLogs()
            webSocketEventDao.clearAllEvents()
        }
    }

    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format("%.1f %s", size, units[unitIndex])
    }
}
