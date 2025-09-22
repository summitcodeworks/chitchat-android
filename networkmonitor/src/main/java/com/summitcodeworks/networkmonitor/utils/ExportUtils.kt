package com.summitcodeworks.networkmonitor.utils

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    data class ExportData(
        val exportTime: String,
        val httpLogs: List<NetworkLog>,
        val webSocketEvents: List<WebSocketEvent>,
        val summary: Map<String, Any>
    )

    /**
     * Export network logs to JSON format
     */
    fun exportToJson(
        context: Context,
        httpLogs: List<NetworkLog>,
        webSocketEvents: List<WebSocketEvent>
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            .format(Date())

        val summary = createSummary(httpLogs, webSocketEvents)

        val exportData = ExportData(
            exportTime = timestamp,
            httpLogs = httpLogs,
            webSocketEvents = webSocketEvents,
            summary = summary
        )

        return gson.toJson(exportData)
    }

    /**
     * Export network logs to a file and share
     */
    fun exportAndShare(
        context: Context,
        httpLogs: List<NetworkLog>,
        webSocketEvents: List<WebSocketEvent>
    ) {
        val jsonData = exportToJson(context, httpLogs, webSocketEvents)

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            .format(Date())
        val fileName = "network_logs_$timestamp.json"

        try {
            val file = File(context.cacheDir, fileName)
            FileWriter(file).use { writer ->
                writer.write(jsonData)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                ))
                putExtra(Intent.EXTRA_SUBJECT, "Network Monitor Logs")
                putExtra(Intent.EXTRA_TEXT, "Network activity logs exported from Network Monitor")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Network Logs")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Export to CSV format
     */
    fun exportToCsv(
        httpLogs: List<NetworkLog>,
        webSocketEvents: List<WebSocketEvent>
    ): String {
        val csvBuilder = StringBuilder()

        // HTTP Logs CSV
        csvBuilder.appendLine("=== HTTP REQUESTS ===")
        csvBuilder.appendLine("Timestamp,Method,URL,Status Code,Duration,Request Size,Response Size,Error")

        httpLogs.forEach { log ->
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(log.requestTime))
            csvBuilder.appendLine(
                "${escapeCSV(timestamp)},${escapeCSV(log.method ?: "")},${escapeCSV(log.url)}," +
                        "${log.responseCode ?: ""},${log.duration ?: ""},${log.requestSize},${log.responseSize}," +
                        "${escapeCSV(log.error ?: "")}"
            )
        }

        csvBuilder.appendLine()
        csvBuilder.appendLine("=== WEBSOCKET EVENTS ===")
        csvBuilder.appendLine("Timestamp,Connection ID,Event Type,URL,Message,Error")

        webSocketEvents.forEach { event ->
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date(event.timestamp))
            csvBuilder.appendLine(
                "${escapeCSV(timestamp)},${escapeCSV(event.connectionId)},${escapeCSV(event.eventType.name)}," +
                        "${escapeCSV(event.url)},${escapeCSV(event.message ?: "")},${escapeCSV(event.error ?: "")}"
            )
        }

        return csvBuilder.toString()
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun createSummary(
        httpLogs: List<NetworkLog>,
        webSocketEvents: List<WebSocketEvent>
    ): Map<String, Any> {
        val httpSuccessful = httpLogs.count { log ->
            log.responseCode != null && log.responseCode in 200..299
        }
        val httpFailed = httpLogs.count { log ->
            log.responseCode != null && log.responseCode >= 400 || log.error != null
        }

        val totalDataTransferred = httpLogs.sumOf { it.requestSize + it.responseSize }
        val averageResponseTime = httpLogs.mapNotNull { it.duration }.average().let {
            if (it.isNaN()) 0.0 else it
        }

        val webSocketConnections = webSocketEvents.distinctBy { it.connectionId }.size
        val webSocketMessages = webSocketEvents.count {
            it.eventType.name.contains("MESSAGE")
        }

        return mapOf(
            "totalHttpRequests" to httpLogs.size,
            "successfulHttpRequests" to httpSuccessful,
            "failedHttpRequests" to httpFailed,
            "totalDataTransferred" to totalDataTransferred,
            "averageResponseTime" to averageResponseTime,
            "webSocketConnections" to webSocketConnections,
            "webSocketMessages" to webSocketMessages,
            "totalWebSocketEvents" to webSocketEvents.size
        )
    }
}