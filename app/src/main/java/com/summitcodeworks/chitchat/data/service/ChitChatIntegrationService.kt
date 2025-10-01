package com.summitcodeworks.chitchat.data.service

import com.summitcodeworks.chitchat.data.remote.websocket.MultiWebSocketManager
import com.summitcodeworks.chitchat.data.repository.AuthRepository
import com.summitcodeworks.chitchat.data.repository.MessageRepository
import com.summitcodeworks.chitchat.data.repository.GroupRepository
import com.summitcodeworks.chitchat.data.repository.NotificationRepository
import com.summitcodeworks.chitchat.data.repository.MediaRepository
import com.summitcodeworks.chitchat.data.remote.error.NetworkErrorHandler
import com.summitcodeworks.chitchat.data.remote.error.withRetry
import com.summitcodeworks.chitchat.data.remote.dto.SendMessageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central service that integrates all ChitChat features
 * This service acts as a facade for the entire application's data layer
 */
@Singleton
class ChitChatIntegrationService @Inject constructor(
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository,
    private val groupRepository: GroupRepository,
    private val notificationRepository: NotificationRepository,
    private val mediaRepository: MediaRepository,
    private val webSocketManager: MultiWebSocketManager,
    private val errorHandler: NetworkErrorHandler
) {
    
    // Authentication
    suspend fun signInWithPhone(phoneNumber: String, otp: String): Result<String> {
        return withRetry(errorHandler) {
            val authResult = authRepository.verifyOtpSms(phoneNumber, otp)

            authResult.fold(
                onSuccess = { authResponse ->
                    authResponse.accessToken
                },
                onFailure = { exception ->
                    throw exception
                }
            )
        }
    }
    
    suspend fun sendOtp(phoneNumber: String): Result<Unit> {
        return withRetry(errorHandler) {
            val result = authRepository.sendOtpSms(phoneNumber)
            result.getOrThrow()
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            authRepository.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Real-time connection
    suspend fun connectWebSocket(token: String) {
        webSocketManager.connectAll()
    }
    
    fun disconnectWebSocket() {
        webSocketManager.disconnectAll()
    }
    
    fun isWebSocketConnected(): Boolean {
        return webSocketManager.isConnected
    }
    
    // Messages
    suspend fun sendMessage(
        token: String,
        receiverId: Long,
        content: String,
        messageType: String = "TEXT"
    ): Result<Unit> {
        return withRetry(errorHandler) {
            val request = SendMessageRequest(
                recipientId = receiverId,
                content = content,
                type = messageType
            )
            val result = messageRepository.sendMessage(token, request)
            result.getOrThrow()
            Unit
        }
    }
    
    suspend fun getConversationMessages(
        token: String,
        userId: Long,
        page: Int = 0,
        limit: Int = 50
    ): Result<List<com.summitcodeworks.chitchat.domain.model.Message>> {
        return withRetry(errorHandler) {
            val result = messageRepository.getConversationMessages(token, userId, page, limit)
            val messagePageResponse = result.getOrThrow()
            messagePageResponse.content.map { messageDto ->
                // Convert MessageDto to domain Message - simplified for now
                com.summitcodeworks.chitchat.domain.model.Message(
                    id = messageDto.id,
                    content = messageDto.content,
                    messageType = com.summitcodeworks.chitchat.domain.model.MessageType.valueOf(messageDto.messageType),
                    senderId = messageDto.senderId,
                    receiverId = messageDto.receiverId,
                    groupId = messageDto.groupId,
                    timestamp = messageDto.timestamp ?: System.currentTimeMillis().toString(),
                    isRead = messageDto.isRead,
                    replyToMessageId = messageDto.replyToMessageId,
                    mediaId = messageDto.mediaId
                )
            }
        }
    }
    
    fun getMessagesFlow(userId: Long): Flow<List<com.summitcodeworks.chitchat.domain.model.Message>> {
        // This method is not available in MessageRepository, simplified for now
        return flowOf(emptyList())
    }
    
    // Groups
    suspend fun createGroup(
        token: String,
        name: String,
        description: String?,
        memberIds: List<Long>
    ): Result<com.summitcodeworks.chitchat.domain.model.Group> {
        return withRetry(errorHandler) {
            groupRepository.createGroup(token, name, description, false, memberIds).getOrThrow()
        }
    }
    
    suspend fun getUserGroups(token: String): Result<List<com.summitcodeworks.chitchat.domain.model.Group>> {
        return withRetry(errorHandler) {
            groupRepository.getUserGroups(token).getOrThrow()
        }
    }
    
    suspend fun joinGroup(token: String, groupId: Long): Result<com.summitcodeworks.chitchat.domain.model.Group> {
        return withRetry(errorHandler) {
            groupRepository.joinGroup(token, groupId).getOrThrow()
        }
    }
    
    fun getUserGroupsFlow(): Flow<List<com.summitcodeworks.chitchat.domain.model.Group>> {
        return groupRepository.getUserGroupsFlow()
    }
    
    // Calls
    suspend fun initiateCall(
        token: String,
        calleeId: Long,
        callType: String
    ): Result<com.summitcodeworks.chitchat.domain.model.Call> {
        return withRetry(errorHandler) {
            // This would use CallRepository
            com.summitcodeworks.chitchat.domain.model.Call(
                sessionId = "mock_session",
                callerId = 1L,
                calleeId = calleeId,
                callType = com.summitcodeworks.chitchat.domain.model.CallType.valueOf(callType.uppercase()),
                status = com.summitcodeworks.chitchat.domain.model.CallStatus.INITIATED,
                startTime = System.currentTimeMillis().toString()
            )
        }
    }
    
    // Notifications
    suspend fun getNotifications(token: String): Result<List<com.summitcodeworks.chitchat.domain.model.Notification>> {
        return withRetry(errorHandler) {
            notificationRepository.getNotifications(token).getOrThrow()
        }
    }
    
    suspend fun markNotificationAsRead(token: String, notificationId: Long): Result<Unit> {
        return withRetry(errorHandler) {
            notificationRepository.markAsRead(token, notificationId).getOrThrow()
        }
    }
    
    suspend fun getUnreadCount(token: String): Result<Int> {
        return withRetry(errorHandler) {
            notificationRepository.getUnreadCount(token).getOrThrow()
        }
    }
    
    fun getNotificationsFlow(): Flow<List<com.summitcodeworks.chitchat.domain.model.Notification>> {
        return notificationRepository.getNotificationsFlow()
    }
    
    fun getUnreadCountFlow(): Flow<Int> {
        return notificationRepository.getUnreadCountFlow()
    }
    
    // Media
    suspend fun uploadMedia(
        token: String,
        file: java.io.File,
        type: String,
        description: String? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<com.summitcodeworks.chitchat.domain.model.Media> {
        return withRetry(errorHandler) {
            mediaRepository.uploadMedia(token, file, type, description, onProgress = onProgress).getOrThrow()
        }
    }
    
    suspend fun getUserMedia(
        token: String,
        userId: Long,
        type: String? = null
    ): Result<List<com.summitcodeworks.chitchat.domain.model.Media>> {
        return withRetry(errorHandler) {
            mediaRepository.getUserMedia(token, userId, type).getOrThrow()
        }
    }
    
    fun getUserMediaFlow(userId: Long): Flow<List<com.summitcodeworks.chitchat.domain.model.Media>> {
        return mediaRepository.getUserMediaFlow(userId)
    }
    
    // WebSocket events
    fun getMessageReceivedFlow(): Flow<com.summitcodeworks.chitchat.data.remote.websocket.MessageReceivedEvent> {
        return webSocketManager.messageReceived
    }
    
    fun getUserTypingFlow(): Flow<com.summitcodeworks.chitchat.data.remote.websocket.UserTypingEvent> {
        return webSocketManager.userTyping
    }
    
    fun getCallEventFlow(): Flow<com.summitcodeworks.chitchat.data.remote.websocket.CallEvent> {
        return webSocketManager.callEvent
    }
    
    fun getGroupEventFlow(): Flow<com.summitcodeworks.chitchat.data.remote.websocket.GroupEvent> {
        // This method is not available in MultiWebSocketManager, simplified for now
        return flowOf()
    }
    
    fun getNotificationEventFlow(): Flow<com.summitcodeworks.chitchat.data.remote.websocket.NotificationEvent> {
        return webSocketManager.notificationEvent
    }
    
    fun getConnectionStateFlow(): Flow<com.summitcodeworks.chitchat.data.remote.websocket.ConnectionState> {
        // This method is not available in MultiWebSocketManager, simplified for now
        return flowOf()
    }
    
    // Combined flows for UI
    fun getChatStateFlow(userId: Long): Flow<ChatState> {
        return combine(
            getMessagesFlow(userId),
            getUserTypingFlow(),
            getConnectionStateFlow()
        ) { messages, typing, connection ->
            ChatState(
                messages = messages,
                isTyping = typing.isTyping && typing.userId == userId,
                isConnected = connection == com.summitcodeworks.chitchat.data.remote.websocket.ConnectionState.CONNECTED
            )
        }
    }
    
    fun getHomeStateFlow(): Flow<HomeState> {
        return combine(
            getUserGroupsFlow(),
            getNotificationsFlow(),
            getUnreadCountFlow(),
            getConnectionStateFlow()
        ) { groups, notifications, unreadCount, connection ->
            HomeState(
                groups = groups,
                notifications = notifications,
                unreadCount = unreadCount,
                isConnected = connection == com.summitcodeworks.chitchat.data.remote.websocket.ConnectionState.CONNECTED
            )
        }
    }
}

data class ChatState(
    val messages: List<com.summitcodeworks.chitchat.domain.model.Message>,
    val isTyping: Boolean,
    val isConnected: Boolean
)

data class HomeState(
    val groups: List<com.summitcodeworks.chitchat.domain.model.Group>,
    val notifications: List<com.summitcodeworks.chitchat.domain.model.Notification>,
    val unreadCount: Int,
    val isConnected: Boolean
)
