package com.summitcodeworks.networkmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_logs")
data class NetworkLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestId: String,
    val type: NetworkType,
    val method: String? = null, // HTTP method (GET, POST, etc.) - null for WebSocket
    val url: String,
    val requestHeaders: String? = null,
    val requestBody: String? = null,
    val responseCode: Int? = null,
    val responseHeaders: String? = null,
    val responseBody: String? = null,
    val requestTime: Long,
    val responseTime: Long? = null,
    val duration: Long? = null,
    val requestSize: Long = 0,
    val responseSize: Long = 0,
    val error: String? = null,
    val protocol: String? = null,
    val isSSL: Boolean = false,
    val curlCommand: String? = null // cURL representation of the request
)

enum class NetworkType {
    HTTP, WEBSOCKET
}

data class NetworkSummary(
    val totalRequests: Int,
    val successfulRequests: Int,
    val failedRequests: Int,
    val totalDataTransferred: Long,
    val averageResponseTime: Long
)