package com.summitcodeworks.chitchat.data.remote.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.google.gson.Gson
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for all WebSocket connections in ChitChat.
 * 
 * This class orchestrates multiple WebSocket connections for different features
 * of the application, providing a unified interface for real-time communication.
 * It manages three specialized WebSocket clients for messaging, calls, and status updates.
 * 
 * WebSocket Clients Managed:
 * - ChitChatWebSocketClient: Handles messaging, typing indicators, and user presence
 * - CallWebSocketClient: Manages voice/video call signaling and events
 * - StatusWebSocketClient: Handles status updates and story sharing
 * 
 * Key responsibilities:
 * - Establish and maintain multiple WebSocket connections
 * - Coordinate connection states across all clients
 * - Provide unified event streams for UI consumption
 * - Handle connection failures and reconnection logic
 * - Manage authentication tokens for all connections
 * - Route messages to appropriate handlers
 * 
 * Real-time features supported:
 * - Instant messaging and message delivery
 * - Typing indicators and read receipts
 * - User online/offline status
 * - Voice/video call signaling
 * - Status updates and story notifications
 * - Push notification coordination
 * 
 * Connection management:
 * - Automatic reconnection on network changes
 * - Graceful degradation when connections fail
 * - Connection state monitoring and reporting
 * - Resource cleanup on app termination
 * 
 * @param messageWebSocket WebSocket client for messaging functionality
 * @param callWebSocket WebSocket client for call management
 * @param statusWebSocket WebSocket client for status updates
 * @param gson JSON serializer for message parsing
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@Singleton
class MultiWebSocketManager @Inject constructor(
    private val messageWebSocket: ChitChatWebSocketClient,
    private val callWebSocket: CallWebSocketClient,
    private val statusWebSocket: StatusWebSocketClient,
    private val gson: Gson
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
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
    
    suspend fun connectAll() {
        messageWebSocket.connect("messages")
        callWebSocket.connect("calls")
        statusWebSocket.connect("status")

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
            recipientId = receiverId,
            groupId = groupId,
            content = content,
            type = messageType,
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
        // Route new messages to MessageReceivedEvent
        scope.launch {
            newMessages.collect { message ->
                try {
                    val data = gson.fromJson(gson.toJson(message.data), com.google.gson.JsonObject::class.java)
                    
                    val messageEvent = MessageReceivedEvent(
                        messageId = data.get("id")?.asString ?: "",
                        senderId = data.get("senderId")?.asLong ?: 0L,
                        receiverId = data.get("receiverId")?.asLong,
                        groupId = data.get("groupId")?.asLong,
                        content = data.get("content")?.asString ?: "",
                        messageType = data.get("type")?.asString ?: "TEXT",
                        timestamp = data.get("timestamp")?.asString ?: System.currentTimeMillis().toString(),
                        isFromCurrentUser = data.get("isFromCurrentUser")?.asBoolean ?: false
                    )
                    
                    _messageReceived.emit(messageEvent)
                } catch (e: Exception) {
                    // Log error but don't crash
                    android.util.Log.e("MultiWebSocketManager", "Error parsing message: ${e.message}")
                }
            }
        }
        
        // Route typing indicators to UserTypingEvent
        scope.launch {
            typingIndicators.collect { message ->
                try {
                    val data = gson.fromJson(gson.toJson(message.data), com.google.gson.JsonObject::class.java)
                    
                    val typingEvent = UserTypingEvent(
                        userId = data.get("userId")?.asLong ?: 0L,
                        receiverId = data.get("receiverId")?.asLong,
                        groupId = data.get("groupId")?.asLong,
                        isTyping = data.get("isTyping")?.asBoolean ?: false,
                        userName = data.get("userName")?.asString
                    )
                    
                    _userTyping.emit(typingEvent)
                } catch (e: Exception) {
                    android.util.Log.e("MultiWebSocketManager", "Error parsing typing indicator: ${e.message}")
                }
            }
        }
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
