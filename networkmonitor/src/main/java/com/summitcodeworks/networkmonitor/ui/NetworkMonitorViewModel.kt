package com.summitcodeworks.networkmonitor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.database.WebSocketEventDao
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkSummary
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworkMonitorViewModel @Inject constructor(
    private val networkLogDao: NetworkLogDao,
    private val webSocketEventDao: WebSocketEventDao
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

    fun exportLogs(): Flow<String> = flow {
        val logs = networkLogDao.getAllLogs().first()
        val events = webSocketEventDao.getAllEvents().first()

        val exportData = mapOf(
            "httpLogs" to logs,
            "webSocketEvents" to events,
            "exportTime" to System.currentTimeMillis()
        )

        // Convert to JSON string for export
        emit(com.google.gson.Gson().toJson(exportData))
    }
}

enum class NetworkMonitorTab {
    HTTP, WEBSOCKET, SUMMARY, FAILED
}