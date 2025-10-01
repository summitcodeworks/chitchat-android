package com.summitcodeworks.networkmonitor.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.database.WebSocketEventDao
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkSummary
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing network monitoring functionality in the NetworkMonitor module.
 * 
 * This ViewModel handles the presentation logic for network debugging features,
 * providing real-time network log monitoring, search capabilities, and performance
 * analytics for the ChitChat application's network requests.
 * 
 * Key responsibilities:
 * - Monitor and display HTTP request/response logs
 * - Track WebSocket events and connections
 * - Provide search and filtering capabilities
 * - Generate network performance summaries
 * - Manage UI state for network monitoring screens
 * - Handle data export and sharing functionality
 * 
 * The ViewModel integrates with local Room database to:
 * - Store and retrieve network logs
 * - Provide real-time updates via Flow
 * - Enable efficient search and filtering
 * - Support data persistence across app sessions
 * 
 * Network monitoring capabilities:
 * - HTTP request/response inspection
 * - WebSocket event tracking
 * - Performance metrics calculation
 * - Error rate monitoring
 * - Response time analysis
 * 
 * @param networkLogDao Data access object for HTTP network logs
 * @param webSocketEventDao Data access object for WebSocket events
 * @param context Application context for various operations
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@HiltViewModel
class NetworkMonitorViewModel @Inject constructor(
    private val networkLogDao: NetworkLogDao,
    private val webSocketEventDao: WebSocketEventDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(NetworkMonitorTab.HTTP)
    val selectedTab: StateFlow<NetworkMonitorTab> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val networkLogs: StateFlow<List<NetworkLog>> = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                networkLogDao.getAllLogs()
            } else {
                networkLogDao.searchLogs(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val webSocketEvents: StateFlow<List<WebSocketEvent>> = webSocketEventDao.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val httpLogs: StateFlow<List<NetworkLog>> = networkLogDao.getLogsByType(NetworkType.HTTP)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val failedRequests: StateFlow<List<NetworkLog>> = networkLogDao.getFailedRequests()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _networkSummary = MutableStateFlow<NetworkSummary?>(null)
    val networkSummary: StateFlow<NetworkSummary?> = _networkSummary.asStateFlow()

    init {
        loadNetworkSummary()
        // Clean up any duplicate logs from old version
        viewModelScope.launch {
            networkLogDao.removeDuplicateLogs()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTab(tab: NetworkMonitorTab) {
        _selectedTab.value = tab
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                networkLogDao.clearAllLogs()
                webSocketEventDao.clearAllEvents()
                loadNetworkSummary()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLog(log: NetworkLog) {
        viewModelScope.launch {
            networkLogDao.deleteLog(log)
            loadNetworkSummary()
        }
    }

    fun deleteWebSocketEvent(event: WebSocketEvent) {
        viewModelScope.launch {
            webSocketEventDao.deleteEvent(event)
        }
    }

    private fun loadNetworkSummary() {
        viewModelScope.launch {
            val summary = networkLogDao.getNetworkSummary(
                System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Last 24 hours
            )
            _networkSummary.value = summary
        }
    }

    fun getLogById(id: Long): Flow<NetworkLog?> = flow {
        emit(networkLogDao.getLogById(id))
    }

    fun getWebSocketEventsByConnection(connectionId: String): Flow<List<WebSocketEvent>> {
        return webSocketEventDao.getEventsByConnectionId(connectionId)
    }

    fun exportLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val logs = networkLogDao.getAllLogs().first()
                val events = webSocketEventDao.getAllEvents().first()
                
                // Use ExportUtils for proper export functionality
                com.summitcodeworks.networkmonitor.utils.ExportUtils.exportAndShare(
                    context = context,
                    httpLogs = logs,
                    webSocketEvents = events
                )
            } catch (e: Exception) {
                // Handle export error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyFilter(filter: NetworkFilter) {
        // TODO: Implement filtering logic
        // This would involve updating the networkLogs flow to apply the filter
        // For now, this is a placeholder
    }

    fun sendRequest(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: String,
        contentType: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a real implementation, you would use OkHttp to send the request
                // For now, we'll create a mock response
                val requestLog = NetworkLog(
                    requestId = java.util.UUID.randomUUID().toString(),
                    type = com.summitcodeworks.networkmonitor.model.NetworkType.HTTP,
                    method = method,
                    url = url,
                    requestHeaders = com.google.gson.Gson().toJson(headers),
                    requestBody = body,
                    requestTime = System.currentTimeMillis(),
                    responseTime = System.currentTimeMillis() + 1000,
                    duration = 1000,
                    responseCode = 200,
                    responseHeaders = "{}",
                    responseBody = "Response from $method $url",
                    requestSize = body.toByteArray().size.toLong(),
                    responseSize = 100L,
                    isSSL = url.startsWith("https://"),
                    protocol = "HTTP/1.1",
                    curlCommand = generateCurlCommand(method, url, headers, body)
                )
                
                networkLogDao.insertLog(requestLog)
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateCurlCommand(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: String
    ): String {
        val curlBuilder = StringBuilder("curl")

        // Add method
        if (method != "GET") {
            curlBuilder.append(" -X $method")
        }

        // Add headers
        headers.forEach { (key, value) ->
            // Properly escape header values
            val escapedValue = value.replace("\"", "\\\"")
            curlBuilder.append(" \\\n  -H \"$key: $escapedValue\"")
        }

        // Add body for POST/PUT/PATCH requests
        if (body.isNotBlank() && method in listOf("POST", "PUT", "PATCH")) {
            val contentType = getContentType(headers)?.lowercase()

            when {
                contentType?.contains("application/json") == true -> {
                    // For JSON, use proper escaping and formatting
                    val escapedBody = body
                        .replace("\\", "\\\\")  // Escape backslashes first
                        .replace("\"", "\\\"")  // Escape double quotes
                        .replace("\n", "\\n")   // Escape newlines
                        .replace("\r", "\\r")   // Escape carriage returns
                        .replace("\t", "\\t")   // Escape tabs
                    curlBuilder.append(" \\\n  -d \"$escapedBody\"")
                }
                contentType?.contains("application/x-www-form-urlencoded") == true -> {
                    // For form data, we can use single quotes safely if no single quotes in data
                    if (body.contains("'")) {
                        val escapedBody = body.replace("\"", "\\\"")
                        curlBuilder.append(" \\\n  -d \"$escapedBody\"")
                    } else {
                        curlBuilder.append(" \\\n  -d '$body'")
                    }
                }
                else -> {
                    // For other content types, try to escape appropriately
                    if (body.contains("\"") && !body.contains("'")) {
                        curlBuilder.append(" \\\n  -d '$body'")
                    } else {
                        val escapedBody = body.replace("\"", "\\\"")
                        curlBuilder.append(" \\\n  -d \"$escapedBody\"")
                    }
                }
            }
        }

        // Add URL (always last)
        curlBuilder.append(" \\\n  \"$url\"")

        return curlBuilder.toString()
    }

    private fun getContentType(headers: Map<String, String>): String? {
        return headers["Content-Type"] ?: headers["content-type"]
    }
}

enum class NetworkMonitorTab {
    HTTP, WEBSOCKET, CURL, SUMMARY, FAILED
}