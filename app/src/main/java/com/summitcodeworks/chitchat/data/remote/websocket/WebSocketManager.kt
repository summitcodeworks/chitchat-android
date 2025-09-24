package com.summitcodeworks.chitchat.data.remote.websocket

import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val webSocketClient: ChitChatWebSocketClient,
    private val environmentManager: EnvironmentManager
) {
    
    // Connection state
    val connectionState: SharedFlow<ConnectionState> = webSocketClient.connectionState
    
    // Message flows
    val newMessages: SharedFlow<WebSocketMessage> = webSocketClient.newMessages
    val typingIndicators: SharedFlow<WebSocketMessage> = webSocketClient.typingIndicators
    val callEvents: SharedFlow<WebSocketMessage> = webSocketClient.callEvents
    val statusUpdates: SharedFlow<WebSocketMessage> = webSocketClient.statusUpdates
    val groupEvents: SharedFlow<WebSocketMessage> = webSocketClient.groupEvents
    val notifications: SharedFlow<WebSocketMessage> = webSocketClient.notifications
    val userPresence: SharedFlow<WebSocketMessage> = webSocketClient.userPresence
    
    // Custom event flows for specific use cases
    private val _messageReceived = MutableSharedFlow<MessageReceivedEvent>()
    val messageReceived: SharedFlow<MessageReceivedEvent> = _messageReceived.asSharedFlow()
    
    private val _userTyping = MutableSharedFlow<UserTypingEvent>()
    val userTyping: SharedFlow<UserTypingEvent> = _userTyping.asSharedFlow()
    
    private val _callEvent = MutableSharedFlow<CallEvent>()
    val callEvent: SharedFlow<CallEvent> = _callEvent.asSharedFlow()
    
    private val _statusEvent = MutableSharedFlow<StatusEvent>()
    val statusEvent: SharedFlow<StatusEvent> = _statusEvent.asSharedFlow()
    
    private val _groupEvent = MutableSharedFlow<GroupEvent>()
    val groupEvent: SharedFlow<GroupEvent> = _groupEvent.asSharedFlow()
    
    private val _notificationEvent = MutableSharedFlow<NotificationEvent>()
    val notificationEvent: SharedFlow<NotificationEvent> = _notificationEvent.asSharedFlow()
    
    suspend fun connect(endpoint: String = "messages") {
        webSocketClient.connect(endpoint)

        // Start listening to WebSocket messages and route them to specific flows
        setupMessageRouting()
    }
    
    fun disconnect() {
        webSocketClient.disconnect()
    }
    
    fun isConnected(): Boolean {
        return webSocketClient.connectionState.value == ConnectionState.CONNECTED
    }
    
    // Message sending methods
    fun sendMessage(
        receiverId: Long? = null,
        groupId: Long? = null,
        content: String,
        messageType: String = "TEXT",
        replyToMessageId: String? = null,
        mediaId: Long? = null
    ) {
        val messageData = SendMessageData(
            receiverId = receiverId,
            groupId = groupId,
            content = content,
            messageType = messageType,
            replyToMessageId = replyToMessageId,
            mediaId = mediaId
        )
        webSocketClient.sendMessageData(messageData)
    }
    
    fun sendTypingIndicator(receiverId: Long, isTyping: Boolean, groupId: Long? = null) {
        webSocketClient.sendTypingIndicator(receiverId, isTyping, groupId)
    }
    
    fun sendMessageRead(messageId: String, senderId: Long, groupId: Long? = null) {
        webSocketClient.sendMessageRead(messageId, senderId, groupId)
    }
    
    fun sendCallSignaling(callData: CallData) {
        webSocketClient.sendCallData(callData)
    }
    
    fun sendStatusUpdate(statusId: Long, userId: Long) {
        val statusData = StatusData(
            userId = userId,
            isOnline = true
        )
        webSocketClient.sendStatusData(statusData)
    }
    
    private fun setupMessageRouting() {
        // This would be implemented with coroutines to listen to the WebSocket flows
        // and route them to specific event flows for easier handling in ViewModels
    }
}

