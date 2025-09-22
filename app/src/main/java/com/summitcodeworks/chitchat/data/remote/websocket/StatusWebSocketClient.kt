package com.summitcodeworks.chitchat.data.remote.websocket

import com.google.gson.Gson
import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusWebSocketClient @Inject constructor(
    private val gson: Gson,
    private val environmentManager: EnvironmentManager
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .build()
    
    private val _messages = MutableSharedFlow<WebSocketMessage>()
    val messages: SharedFlow<WebSocketMessage> = _messages.asSharedFlow()
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // Status-specific flows
    private val _statusEvents = MutableSharedFlow<StatusWebSocketMessage>()
    val statusEvents: SharedFlow<StatusWebSocketMessage> = _statusEvents.asSharedFlow()
    
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    
    fun connect(token: String, url: String = "") {
        val webSocketUrl = if (url.isNotEmpty()) url else "${environmentManager.getCurrentWebSocketBaseUrl()}ws/status"
        if (_connectionState.value == ConnectionState.CONNECTED) {
            return
        }
        
        _connectionState.value = ConnectionState.CONNECTING
        
        val request = Request.Builder()
            .url(webSocketUrl)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                
                // Send authentication
                val authMessage = AuthMessage(token = token)
                sendMessage(authMessage)
                
                // Start heartbeat
                startHeartbeat()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, WebSocketMessage::class.java)
                    _messages.tryEmit(message)
                    
                    // Route status-specific messages
                    when (message.type) {
                        WebSocketMessageType.NEW_STATUS,
                        WebSocketMessageType.STATUS_VIEWED,
                        WebSocketMessageType.STATUS_REACTED,
                        WebSocketMessageType.STATUS_UPDATE -> {
                            try {
                                val statusMessage = gson.fromJson(text, StatusWebSocketMessage::class.java)
                                _statusEvents.tryEmit(statusMessage)
                            } catch (e: Exception) {
                                // Handle parsing error
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Handle JSON parsing error
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Handle binary messages if needed
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTING
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTED
                stopHeartbeat()
                scheduleReconnect(token, url)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.DISCONNECTED
                stopHeartbeat()
                scheduleReconnect(token, url)
            }
        })
    }
    
    fun disconnect() {
        reconnectJob?.cancel()
        stopHeartbeat()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }
    
    fun sendMessage(message: Any) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            try {
                val json = gson.toJson(message)
                webSocket?.send(json)
            } catch (e: Exception) {
                // Handle send error
            }
        }
    }
    
    fun sendStatusData(data: StatusData) {
        val message = WebSocketMessage(
            type = WebSocketMessageType.STATUS_UPDATE,
            data = data
        )
        sendMessage(message)
    }
    
    fun sendStatusView(statusId: Long) {
        val message = WebSocketMessage(
            type = WebSocketMessageType.STATUS_VIEWED,
            data = mapOf("statusId" to statusId)
        )
        sendMessage(message)
    }
    
    fun sendStatusReaction(statusId: Long, reaction: String) {
        val message = WebSocketMessage(
            type = WebSocketMessageType.STATUS_REACTED,
            data = mapOf(
                "statusId" to statusId,
                "reaction" to reaction
            )
        )
        sendMessage(message)
    }
    
    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                delay(30000) // Send heartbeat every 30 seconds
                webSocket?.send("ping")
            }
        }
    }
    
    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }
    
    private fun scheduleReconnect(token: String, url: String) {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(5000) // Wait 5 seconds before reconnecting
            if (_connectionState.value == ConnectionState.DISCONNECTED) {
                connect(token, url)
            }
        }
    }
}

// Status-specific WebSocket message
data class StatusWebSocketMessage(
    val type: String,
    val data: StatusData? = null,
    val statusId: Long? = null,
    val userId: Long? = null,
    val reaction: String? = null,
    val timestamp: String? = null
)
