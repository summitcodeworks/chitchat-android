package com.summitcodeworks.chitchat.data.remote.websocket

import com.google.gson.Gson
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.*
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallWebSocketClient @Inject constructor(
    private val gson: Gson,
    private val environmentManager: EnvironmentManager,
    private val otpAuthManager: OtpAuthManager
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .build()
    
    private val _messages = MutableSharedFlow<WebSocketMessage>()
    val messages: SharedFlow<WebSocketMessage> = _messages.asSharedFlow()
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // Call-specific flows
    private val _callEvents = MutableSharedFlow<CallWebSocketMessage>()
    val callEvents: SharedFlow<CallWebSocketMessage> = _callEvents.asSharedFlow()
    
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    
    suspend fun connect(endpoint: String = "calls") {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            return
        }

        _connectionState.value = ConnectionState.CONNECTING

        try {
            // Get OTP token automatically
            val otpToken = otpAuthManager.getCurrentToken()
            if (otpToken == null) {
                _connectionState.value = ConnectionState.DISCONNECTED
                return
            }

            // Create WebSocket URL with token as query parameter
            val baseUrl = environmentManager.getCurrentWebSocketBaseUrl()
            val webSocketUrl = "${baseUrl}ws/$endpoint?token=$otpToken"

            val request = Request.Builder()
                .url(webSocketUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _connectionState.value = ConnectionState.CONNECTED

                    // Start heartbeat
                    startHeartbeat()
                }
            
                override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, WebSocketMessage::class.java)
                    _messages.tryEmit(message)
                    
                    // Route call-specific messages
                    when (message.type) {
                        WebSocketMessageType.CALL_INITIATED,
                        WebSocketMessageType.CALL_ANSWERED,
                        WebSocketMessageType.CALL_REJECTED,
                        WebSocketMessageType.CALL_ENDED,
                        WebSocketMessageType.CALL_RINGING -> {
                            try {
                                val callMessage = gson.fromJson(text, CallWebSocketMessage::class.java)
                                _callEvents.tryEmit(callMessage)
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
                // Handle binary messages (WebRTC signaling)
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.DISCONNECTING
            }
            
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    stopHeartbeat()
                    scheduleReconnect(endpoint)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    stopHeartbeat()
                    scheduleReconnect(endpoint)
                }
            })

        } catch (e: Exception) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
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
    
    fun sendCallSignaling(data: CallData) {
        val message = WebSocketMessage(
            type = data.callType,
            data = data
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
    
    private fun scheduleReconnect(endpoint: String) {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(5000) // Wait 5 seconds before reconnecting
            if (_connectionState.value == ConnectionState.DISCONNECTED) {
                connect(endpoint)
            }
        }
    }
}

