package com.summitcodeworks.chitchat.data.remote.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for all WebSocket connections
 * Manages messaging, calls, and status WebSocket clients
 */
@Singleton
class MultiWebSocketManager @Inject constructor(
    private val messageWebSocket: ChitChatWebSocketClient,
    private val callWebSocket: CallWebSocketClient,
    private val statusWebSocket: StatusWebSocketClient
) {
    
    // Combined connection state
    val isConnected: Boolean
        get() = messageWebSocket.connectionState.value == ConnectionState.CONNECTED &&
                callWebSocket.connectionState.value == ConnectionState.CONNECTED &&
                statusWebSocket.connectionState.value == ConnectionState.CONNECTED
    
    // Individual connection states
    val messageConnectionState: SharedFlow<ConnectionState> = messageWebSocket.connectionState
    val callConnectionState: SharedFlow<ConnectionState> = callWebSocket.connectionState
    val statusConnectionState: SharedFlow<ConnectionState> = statusWebSocket.connectionState
    
    // Message flows
    val newMessages: SharedFlow<WebSocketMessage> = messageWebSocket.newMessages
    val typingIndicators: SharedFlow<WebSocketMessage> = messageWebSocket.typingIndicators
    val userPresence: SharedFlow<WebSocketMessage> = messageWebSocket.userPresence
    val notifications: SharedFlow<WebSocketMessage> = messageWebSocket.notifications
    
    // Call flows
    val callEvents: SharedFlow<CallWebSocketMessage> = callWebSocket.callEvents
    
    // Status flows
    val statusEvents: SharedFlow<StatusWebSocketMessage> = statusWebSocket.statusEvents
    
    // Custom event flows for specific use cases
    private val _messageReceived = MutableSharedFlow<MessageReceivedEvent>()
    val messageReceived: SharedFlow<MessageReceivedEvent> = _messageReceived.asSharedFlow()
    
    private val _userTyping = MutableSharedFlow<UserTypingEvent>()
    val userTyping: SharedFlow<UserTypingEvent> = _userTyping.asSharedFlow()
    
    private val _callEvent = MutableSharedFlow<CallEvent>()
    val callEvent: SharedFlow<CallEvent> = _callEvent.asSharedFlow()
    
    private val _statusEvent = MutableSharedFlow<StatusEvent>()
    val statusEvent: SharedFlow<StatusEvent> = _statusEvent.asSharedFlow()
    
    private val _notificationEvent = MutableSharedFlow<NotificationEvent>()
    val notificationEvent: SharedFlow<NotificationEvent> = _notificationEvent.asSharedFlow()
    
    fun connectAll(token: String) {
        messageWebSocket.connect(token)
        callWebSocket.connect(token)
        statusWebSocket.connect(token)
        
        // Start listening to WebSocket messages and route them to specific flows
        setupMessageRouting()
    }
    
    fun disconnectAll() {
        messageWebSocket.disconnect()
        callWebSocket.disconnect()
        statusWebSocket.disconnect()
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
        messageWebSocket.sendMessageData(messageData)
    }
    
    fun sendTypingIndicator(receiverId: Long, isTyping: Boolean, groupId: Long? = null) {
        messageWebSocket.sendTypingIndicator(receiverId, isTyping, groupId)
    }
    
    fun sendMessageRead(messageId: String, senderId: Long, groupId: Long? = null) {
        messageWebSocket.sendMessageRead(messageId, senderId, groupId)
    }
    
    // Call signaling methods
    fun sendCallSignaling(callData: CallData) {
        callWebSocket.sendCallSignaling(callData)
    }
    
    // Status methods
    fun sendStatusUpdate(statusId: Long, userId: Long) {
        val statusData = StatusData(
            userId = userId,
            isOnline = true
        )
        statusWebSocket.sendStatusData(statusData)
    }
    
    fun sendStatusView(statusId: Long) {
        statusWebSocket.sendStatusView(statusId)
    }
    
    fun sendStatusReaction(statusId: Long, reaction: String) {
        statusWebSocket.sendStatusReaction(statusId, reaction)
    }
    
    private fun setupMessageRouting() {
        // This would be implemented with coroutines to listen to the WebSocket flows
        // and route them to specific event flows for easier handling in ViewModels
        // For now, this is a placeholder for the routing logic
    }
}

// Event data classes for specific WebSocket message types
data class MessageReceivedEvent(
    val messageId: String,
    val senderId: Long,
    val receiverId: Long?,
    val groupId: Long?,
    val content: String,
    val messageType: String,
    val timestamp: String,
    val isFromCurrentUser: Boolean = false
)

data class UserTypingEvent(
    val userId: Long,
    val receiverId: Long?,
    val groupId: Long?,
    val isTyping: Boolean,
    val userName: String? = null
)

data class CallEvent(
    val type: String, // "initiated", "answered", "rejected", "ended", "ringing"
    val sessionId: String,
    val callerId: Long,
    val calleeId: Long,
    val callType: String, // "VOICE", "VIDEO"
    val data: String? = null
)

data class StatusEvent(
    val type: String, // "new_status", "status_viewed", "status_reaction"
    val statusId: Long,
    val userId: Long,
    val data: String? = null
)

data class GroupEvent(
    val type: String, // "created", "updated", "member_added", "member_removed", "left"
    val groupId: Long,
    val groupName: String?,
    val affectedUserId: Long?,
    val affectedBy: Long?
)

data class NotificationEvent(
    val notificationId: Long,
    val type: String,
    val title: String,
    val body: String,
    val data: String? = null
)
