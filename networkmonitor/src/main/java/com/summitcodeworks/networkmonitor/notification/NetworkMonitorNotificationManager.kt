package com.summitcodeworks.networkmonitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.summitcodeworks.networkmonitor.R
import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.database.WebSocketEventDao
import com.summitcodeworks.networkmonitor.model.NetworkSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitorNotificationManager @Inject constructor(
    private val context: Context,
    private val networkLogDao: NetworkLogDao,
    private val webSocketEventDao: WebSocketEventDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val notificationManager = NotificationManagerCompat.from(context)
    private val requestCounter = AtomicInteger(0)
    private val wsEventCounter = AtomicInteger(0)

    companion object {
        private const val CHANNEL_ID = "network_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE = 100
    }

    init {
        createNotificationChannel()
        startMonitoring()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Network Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows network activity summary"
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMonitoring() {
        scope.launch {
            combine(
                networkLogDao.getAllLogs(),
                webSocketEventDao.getAllEvents()
            ) { logs, events ->
                Pair(logs, events)
            }.collect { (logs, events) ->
                updateNotification(logs.size, events.size)
            }
        }
    }

    private fun updateNotification(httpRequests: Int, webSocketEvents: Int) {
        val intent = createLaunchIntent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_network_monitor)
            .setContentTitle("Network Monitor")
            .setContentText("HTTP: $httpRequests | WebSocket: $webSocketEvents")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    private fun createLaunchIntent(): Intent {
        // Create intent to launch NetworkMonitor UI
        return context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent().apply {
                setClassName(context, "com.summitcodeworks.networkmonitor.ui.NetworkMonitorActivity")
            }
    }

    fun showActivityNotification(summary: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_network_monitor)
            .setContentTitle("Network Activity")
            .setContentText(summary)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    fun hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun updateSummaryNotification(summary: NetworkSummary?) {
        if (summary == null) return

        val summaryText = "Requests: ${summary.totalRequests} | " +
                "Success: ${summary.successfulRequests} | " +
                "Failed: ${summary.failedRequests}"

        val intent = createLaunchIntent()
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_network_monitor)
            .setContentTitle("Network Monitor Summary")
            .setContentText(summaryText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Total Requests: ${summary.totalRequests}\n" +
                        "Successful: ${summary.successfulRequests}\n" +
                        "Failed: ${summary.failedRequests}\n" +
                        "Data Transferred: ${formatBytes(summary.totalDataTransferred)}\n" +
                        "Avg Response Time: ${summary.averageResponseTime}ms"
            ))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
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