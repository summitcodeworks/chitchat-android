package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Message
import com.summitcodeworks.chitchat.domain.model.MessageType
import com.summitcodeworks.chitchat.domain.usecase.message.GetConversationMessagesUseCase
import com.summitcodeworks.chitchat.domain.usecase.message.SendMessageUseCase
import com.summitcodeworks.chitchat.presentation.state.ChatState
import com.summitcodeworks.chitchat.data.remote.websocket.MultiWebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing chat functionality in ChitChat.
 * 
 * This ViewModel handles all aspects of a chat conversation including:
 * - Sending and receiving messages in real-time via WebSocket
 * - Loading conversation history from local database and remote API
 * - Managing typing indicators between users
 * - Tracking read receipts and message delivery status
 * - Handling user profile information for the chat partner
 * 
 * The ViewModel integrates with multiple data sources:
 * - WebSocket connection for real-time messaging
 * - Local Room database for offline message storage
 * - Remote API for message persistence and user data
 * - OTP authentication manager for user identification
 * 
 * Key features:
 * - Real-time message synchronization via WebSocket
 * - Automatic message deduplication to prevent duplicates
 * - Typing indicator management
 * - Conversation read status tracking
 * - User profile loading and caching
 * 
 * @param sendMessageUseCase Use case for sending messages to the server
 * @param getConversationMessagesUseCase Use case for loading conversation history
 * @param webSocketManager Manager for WebSocket connections and real-time events
 * @param userDao Data access object for local user data
 * @param messageDao Data access object for local message data
 * @param getUserByIdUseCase Use case for fetching user details by ID
 * @param otpAuthManager Authentication manager for current user identification
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase,
    private val webSocketManager: MultiWebSocketManager,
    private val userDao: com.summitcodeworks.chitchat.data.local.dao.UserDao,
    private val messageDao: com.summitcodeworks.chitchat.data.local.dao.MessageDao,
    private val getUserByIdUseCase: com.summitcodeworks.chitchat.domain.usecase.user.GetUserByIdUseCase,
    private val otpAuthManager: com.summitcodeworks.chitchat.data.auth.OtpAuthManager
) : ViewModel() {
    
    // Chat state containing messages, loading status, and errors
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
    // Typing indicator state - Pair of (userId, isTyping)
    private val _isOtherUserTyping = MutableStateFlow<Pair<Long, Boolean>>(Pair(0L, false))
    val isOtherUserTyping: StateFlow<Pair<Long, Boolean>> = _isOtherUserTyping.asStateFlow()
    
    // Other user's display name for the chat header
    private val _otherUserName = MutableStateFlow<String?>(null)
    val otherUserName: StateFlow<String?> = _otherUserName.asStateFlow()
    
    // Other user's phone number for display
    private val _otherUserPhone = MutableStateFlow<String?>(null)
    val otherUserPhone: StateFlow<String?> = _otherUserPhone.asStateFlow()
    
    // Complete other user details for profile information
    private val _otherUserDetails = MutableStateFlow<com.summitcodeworks.chitchat.domain.model.User?>(null)
    val otherUserDetails: StateFlow<com.summitcodeworks.chitchat.domain.model.User?> = _otherUserDetails.asStateFlow()
    
    // ID of the user we're currently chatting with
    private var currentUserId: Long? = null
    
    init {
        // Initialize WebSocket observers for real-time features
        observeTypingEvents()
        observeIncomingMessages()
    }
    
    /**
     * Observes typing events from WebSocket to show typing indicators.
     * 
     * This method listens to real-time typing events from other users and updates
     * the typing state accordingly. The typing indicator shows when another user
     * is currently typing a message.
     */
    private fun observeTypingEvents() {
        viewModelScope.launch {
            webSocketManager.userTyping.collect { typingEvent ->
                // Update typing state for the specific user
                _isOtherUserTyping.value = Pair(typingEvent.userId, typingEvent.isTyping)
            }
        }
    }
    
    /**
     * Observes incoming messages from WebSocket for real-time message updates.
     * 
     * This method listens to real-time message events and adds them to the current
     * conversation if they are relevant. It prevents duplicate messages by checking
     * if the message already exists in the current message list.
     * 
     * Message filtering logic:
     * - For incoming messages: senderId matches currentUserId and not from current user
     * - For outgoing messages: receiverId matches currentUserId and from current user
     */
    private fun observeIncomingMessages() {
        viewModelScope.launch {
            webSocketManager.messageReceived.collect { messageEvent ->
                // Only add message if it's for the current conversation
                if (currentUserId != null) {
                    val isRelevantMessage = 
                        (messageEvent.senderId == currentUserId && !messageEvent.isFromCurrentUser) ||
                        (messageEvent.receiverId == currentUserId && messageEvent.isFromCurrentUser)
                    
                    if (isRelevantMessage) {
                        // Convert WebSocket message event to domain Message model
                        val message = Message(
                            id = messageEvent.messageId,
                            senderId = messageEvent.senderId,
                            receiverId = messageEvent.receiverId,
                            groupId = messageEvent.groupId,
                            content = messageEvent.content,
                            messageType = MessageType.valueOf(messageEvent.messageType),
                            timestamp = messageEvent.timestamp,
                            isRead = false,
                            isDelivered = true
                        )
                        
                        // Add to messages if not already exists (prevent duplicates)
                        val currentMessages = _chatState.value.messages
                        if (currentMessages.none { it.id == message.id }) {
                            _chatState.value = _chatState.value.copy(
                                messages = currentMessages + message
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sets the current conversation with a specific user.
     * 
     * This method is called when the user opens a chat with another user.
     * It updates the current user ID and marks all messages in the conversation
     * as read to update the unread count.
     * 
     * @param userId The ID of the user to start/continue conversation with
     */
    fun setCurrentConversation(userId: Long) {
        currentUserId = userId
        markConversationAsRead(userId)
    }
    
    /**
     * Marks all messages in a conversation as read.
     * 
     * This method updates the read status of all messages between the current user
     * and the specified sender. It's called when the user opens a conversation
     * to update the unread message count.
     * 
     * @param senderId The ID of the user whose messages should be marked as read
     */
    private fun markConversationAsRead(senderId: Long) {
        viewModelScope.launch {
            try {
                val currentUser = otpAuthManager.getCurrentUserId() ?: return@launch
                // Mark all messages from this sender as read
                messageDao.markConversationAsRead(currentUser, senderId)
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "Error marking conversation as read: ${e.message}")
            }
        }
    }
    
    /**
     * Sends a message to a user or group.
     * 
     * This method handles sending various types of messages including text, media,
     * and reply messages. It updates the UI state to show loading and adds the
     * sent message to the current conversation.
     * 
     * @param token Authentication token for the API request
     * @param receiverId ID of the user to send the message to (for direct messages)
     * @param groupId ID of the group to send the message to (for group messages)
     * @param content The message content (text or media description)
     * @param messageType Type of message (TEXT, IMAGE, VIDEO, etc.)
     * @param replyToMessageId ID of the message being replied to (for replies)
     * @param mediaId ID of the media file being sent (for media messages)
     */
    fun sendMessage(
        token: String,
        receiverId: Long? = null,
        groupId: Long? = null,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        replyToMessageId: String? = null,
        mediaId: Long? = null
    ) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true, error = null)
            
            sendMessageUseCase(token, receiverId, groupId, content, messageType, replyToMessageId, mediaId)
                .fold(
                    onSuccess = { message ->
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            messages = _chatState.value.messages + message
                        )
                    },
                    onFailure = { exception ->
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
        }
    }
    
    fun loadConversationMessages(token: String, userId: Long, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true, error = null)
            
            getConversationMessagesUseCase(token, userId, page, size)
                .fold(
                    onSuccess = { messages ->
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            messages = messages
                        )
                    },
                    onFailure = { exception ->
                        _chatState.value = _chatState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
        }
    }
    
    fun observeLocalMessages(senderId: Long, receiverId: Long) {
        viewModelScope.launch {
            getConversationMessagesUseCase.observeLocalMessages(senderId, receiverId)
                .collect { messages ->
                    _chatState.value = _chatState.value.copy(messages = messages)
                }
        }
    }
    
    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
    }
    
    fun clearMessages() {
        _chatState.value = _chatState.value.copy(messages = emptyList())
    }
    
    fun sendTypingIndicator(receiverId: Long, isTyping: Boolean, groupId: Long? = null) {
        webSocketManager.sendTypingIndicator(receiverId, isTyping, groupId)
    }
    
    fun loadUserName(userId: Long) {
        viewModelScope.launch {
            try {
                // First try to get from local database
                var user = userDao.getUserById(userId)
                
                // If not in local DB, fetch from API
                if (user == null) {
                    getUserByIdUseCase(userId).fold(
                        onSuccess = { userDto ->
                            // User has been saved to local DB by the use case
                            user = userDao.getUserById(userId)
                        },
                        onFailure = { 
                            // API call failed, user remains null
                        }
                    )
                }
                
                _otherUserName.value = user?.name
                _otherUserPhone.value = user?.phoneNumber
                
                // Convert to domain User model for profile
                user?.let { userEntity ->
                    _otherUserDetails.value = com.summitcodeworks.chitchat.domain.model.User(
                        id = userEntity.id,
                        phoneNumber = userEntity.phoneNumber,
                        name = userEntity.name,
                        avatarUrl = userEntity.avatarUrl,
                        about = userEntity.about,
                        lastSeen = userEntity.lastSeen,
                        isOnline = userEntity.isOnline,
                        createdAt = userEntity.createdAt,
                        isBlocked = userEntity.isBlocked,
                        isContact = userEntity.isContact
                    )
                }
            } catch (e: Exception) {
                // If any error occurs, fallback to null
                _otherUserName.value = null
                _otherUserPhone.value = null
                _otherUserDetails.value = null
            }
        }
    }
}
