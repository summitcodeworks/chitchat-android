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
class ChitChatWebSocketClient @Inject constructor(
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
    
    // Message type specific flows
    private val _newMessages = MutableSharedFlow<WebSocketMessage>()
    val newMessages: SharedFlow<WebSocketMessage> = _newMessages.asSharedFlow()
    
    private val _typingIndicators = MutableSharedFlow<WebSocketMessage>()
    val typingIndicators: SharedFlow<WebSocketMessage> = _typingIndicators.asSharedFlow()
    
    private val _callEvents = MutableSharedFlow<WebSocketMessage>()
    val callEvents: SharedFlow<WebSocketMessage> = _callEvents.asSharedFlow()
    
    private val _statusUpdates = MutableSharedFlow<WebSocketMessage>()
    val statusUpdates: SharedFlow<WebSocketMessage> = _statusUpdates.asSharedFlow()
    
    private val _groupEvents = MutableSharedFlow<WebSocketMessage>()
    val groupEvents: SharedFlow<WebSocketMessage> = _groupEvents.asSharedFlow()
    
    private val _notifications = MutableSharedFlow<WebSocketMessage>()
    val notifications: SharedFlow<WebSocketMessage> = _notifications.asSharedFlow()
    
    private val _userPresence = MutableSharedFlow<WebSocketMessage>()
    val userPresence: SharedFlow<WebSocketMessage> = _userPresence.asSharedFlow()
    
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    
    suspend fun connect(endpoint: String = "messages") {
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

            // Create WebSocket URL with token as query parameter (as per API docs)
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
                    
                    // Route message to specific flows based on type
                    routeMessage(message)
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
    
    fun sendTypingIndicator(receiverId: Long, isTyping: Boolean, groupId: Long? = null) {
        val typingMessage = WebSocketMessage(
            type = WebSocketMessageType.TYPING,
            data = TypingData(receiverId = receiverId, isTyping = isTyping)
        )
        sendMessage(typingMessage)
    }
    
    fun sendMessageData(data: SendMessageData) {
        val message = WebSocketMessage(
            type = WebSocketMessageType.SEND_MESSAGE,
            data = data
        )
        sendMessage(message)
    }
    
    fun sendCallData(data: CallData) {
        val message = WebSocketMessage(
            type = data.callType,
            data = data
        )
        sendMessage(message)
    }
    
    fun sendUserPresence(isOnline: Boolean) {
        val message = WebSocketMessage(
            type = if (isOnline) WebSocketMessageType.USER_ONLINE else WebSocketMessageType.USER_OFFLINE,
            data = mapOf("isOnline" to isOnline)
        )
        sendMessage(message)
    }
    
    fun sendGroupEvent(eventType: String, data: Map<String, Any>) {
        val message = WebSocketMessage(
            type = eventType,
            data = data
        )
        sendMessage(message)
    }
    
    private fun routeMessage(message: WebSocketMessage) {
        when (message.type) {
            WebSocketMessageType.NEW_MESSAGE,
            WebSocketMessageType.MESSAGE_READ,
            WebSocketMessageType.MESSAGE_DELIVERED,
            WebSocketMessageType.MESSAGE_DELETED -> {
                _newMessages.tryEmit(message)
            }
            WebSocketMessageType.USER_TYPING,
            WebSocketMessageType.TYPING -> {
                _typingIndicators.tryEmit(message)
            }
            WebSocketMessageType.CALL_INITIATED,
            WebSocketMessageType.CALL_ANSWERED,
            WebSocketMessageType.CALL_REJECTED,
            WebSocketMessageType.CALL_ENDED,
            WebSocketMessageType.CALL_RINGING -> {
                _callEvents.tryEmit(message)
            }
            WebSocketMessageType.NEW_STATUS,
            WebSocketMessageType.STATUS_VIEWED,
            WebSocketMessageType.STATUS_REACTED,
            WebSocketMessageType.STATUS_UPDATE -> {
                _statusUpdates.tryEmit(message)
            }
            WebSocketMessageType.GROUP_CREATED,
            WebSocketMessageType.GROUP_UPDATED,
            WebSocketMessageType.GROUP_MEMBER_ADDED,
            WebSocketMessageType.GROUP_MEMBER_REMOVED,
            WebSocketMessageType.GROUP_LEFT -> {
                _groupEvents.tryEmit(message)
            }
            WebSocketMessageType.NOTIFICATION -> {
                _notifications.tryEmit(message)
            }
            WebSocketMessageType.USER_ONLINE,
            WebSocketMessageType.USER_OFFLINE -> {
                _userPresence.tryEmit(message)
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
    
    fun sendMessageRead(messageId: String, senderId: Long, groupId: Long? = null) {
        val message = WebSocketMessage(
            type = WebSocketMessageType.MESSAGE_READ,
            data = mapOf(
                "messageId" to messageId,
                "senderId" to senderId,
                "groupId" to groupId
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
