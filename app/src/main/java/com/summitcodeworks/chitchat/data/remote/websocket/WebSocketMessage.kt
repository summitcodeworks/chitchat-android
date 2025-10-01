package com.summitcodeworks.chitchat.data.remote.websocket

data class WebSocketMessage(
    val type: String,
    val data: Any? = null
)

data class AuthMessage(
    val type: String = "AUTH",
    val token: String
)

data class SendMessageData(
    val recipientId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    val type: String = "TEXT",
    val replyToMessageId: String? = null,
    val mediaId: Long? = null
)

data class TypingData(
    val receiverId: Long,
    val isTyping: Boolean
)

data class CallData(
    val sessionId: String,
    val callerId: Long,
    val calleeId: Long,
    val callType: String,
    val sdpOffer: String? = null,
    val sdpAnswer: String? = null,
    val iceCandidate: String? = null
)

data class StatusData(
    val userId: Long,
    val isOnline: Boolean,
    val lastSeen: String? = null
)

// Call-specific WebSocket message
data class CallWebSocketMessage(
    val type: String,
    val data: CallData? = null,
    val sessionId: String? = null,
    val callerId: Long? = null,
    val calleeId: Long? = null,
    val callType: String? = null,
    val status: String? = null,
    val timestamp: String? = null
)

// WebSocket Message Types
object WebSocketMessageType {
    const val AUTH = "AUTH"
    const val AUTH_SUCCESS = "AUTH_SUCCESS"
    const val AUTH_FAILED = "AUTH_FAILED"
    
    const val NEW_MESSAGE = "NEW_MESSAGE"
    const val SEND_MESSAGE = "SEND_MESSAGE"
    const val MESSAGE_READ = "MESSAGE_READ"
    const val MESSAGE_DELIVERED = "MESSAGE_DELIVERED"
    const val MESSAGE_DELETED = "MESSAGE_DELETED"
    
    const val USER_TYPING = "USER_TYPING"
    const val TYPING = "TYPING"
    
    const val CALL_INITIATED = "CALL_INITIATED"
    const val CALL_ANSWERED = "CALL_ANSWERED"
    const val CALL_REJECTED = "CALL_REJECTED"
    const val CALL_ENDED = "CALL_ENDED"
    const val CALL_RINGING = "CALL_RINGING"
    
    const val USER_ONLINE = "USER_ONLINE"
    const val USER_OFFLINE = "USER_OFFLINE"
    const val STATUS_UPDATE = "STATUS_UPDATE"
    
    const val NEW_STATUS = "NEW_STATUS"
    const val STATUS_VIEWED = "STATUS_VIEWED"
    const val STATUS_REACTED = "STATUS_REACTED"
    
    const val GROUP_CREATED = "GROUP_CREATED"
    const val GROUP_UPDATED = "GROUP_UPDATED"
    const val GROUP_MEMBER_ADDED = "GROUP_MEMBER_ADDED"
    const val GROUP_MEMBER_REMOVED = "GROUP_MEMBER_REMOVED"
    const val GROUP_LEFT = "GROUP_LEFT"
    
    const val NOTIFICATION = "NOTIFICATION"
    const val ERROR = "ERROR"
}

// WebSocket Connection State
enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}
