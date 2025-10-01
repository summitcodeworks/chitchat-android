package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Conversation
import com.summitcodeworks.chitchat.data.remote.websocket.MultiWebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the conversations list in ChitChat.
 * 
 * This ViewModel handles the main conversations screen functionality including:
 * - Loading existing conversations from local database
 * - Real-time updates when new messages arrive via WebSocket
 * - Managing conversation metadata (unread counts, last messages, user info)
 * - Adding, updating, and removing conversations
 * - Fetching user details for conversation participants
 * 
 * The conversations list shows all users with whom the current user has exchanged
 * messages, sorted by most recent activity. Each conversation displays:
 * - User name and avatar
 * - Last message content and timestamp
 * - Unread message count
 * - Online status of the other user
 * 
 * Key features:
 * - Real-time conversation updates via WebSocket
 * - Automatic user detail fetching and caching
 * - Conversation sorting by most recent activity
 * - Unread count management
 * - Conversation lifecycle management
 * 
 * @param webSocketManager Manager for real-time message updates
 * @param userDao Data access object for local user data
 * @param messageDao Data access object for local message data
 * @param otpAuthManager Authentication manager for current user identification
 * @param getUserByIdUseCase Use case for fetching user details by ID
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val webSocketManager: MultiWebSocketManager,
    private val userDao: com.summitcodeworks.chitchat.data.local.dao.UserDao,
    private val messageDao: com.summitcodeworks.chitchat.data.local.dao.MessageDao,
    private val otpAuthManager: com.summitcodeworks.chitchat.data.auth.OtpAuthManager,
    private val getUserByIdUseCase: com.summitcodeworks.chitchat.domain.usecase.user.GetUserByIdUseCase
) : ViewModel() {
    
    // List of all conversations sorted by most recent activity
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    init {
        // Initialize conversations list and real-time updates
        loadExistingConversations()
        observeNewMessages()
    }
    
    /**
     * Loads existing conversations from the local database.
     * 
     * This method fetches all messages for the current user, groups them by
     * conversation partner, and creates Conversation objects with the latest
     * message and user details. It also fetches missing user information
     * from the API when needed.
     */
    private fun loadExistingConversations() {
        viewModelScope.launch {
            val currentUserId = otpAuthManager.getCurrentUserId() ?: return@launch
            
            messageDao.getAllUserMessages(currentUserId).collect { messages ->
                // Group messages by conversation (other user ID)
                val conversationsMap = mutableMapOf<Long, MutableList<com.summitcodeworks.chitchat.data.local.entity.MessageEntity>>()
                
                messages.forEach { message ->
                    val otherUserId = if (message.senderId == currentUserId) {
                        message.receiverId
                    } else {
                        message.senderId
                    }
                    
                    if (otherUserId != null && otherUserId != currentUserId) {
                        conversationsMap.getOrPut(otherUserId) { mutableListOf() }.add(message)
                    }
                }
                
                // Convert to Conversation objects and fetch missing user details
                val conversationsList = conversationsMap.map { (userId, msgs) ->
                    val latestMessage = msgs.maxByOrNull { it.timestamp }
                    var user = userDao.getUserById(userId)
                    
                    // If user not in local DB, fetch from API
                    if (user == null) {
                        getUserByIdUseCase(userId).fold(
                            onSuccess = { userDto ->
                                user = userDao.getUserById(userId)
                            },
                            onFailure = { 
                                // API call failed, user remains null
                            }
                        )
                    }
                    
                    val unreadCount = msgs.count { !it.isRead && it.receiverId == currentUserId }
                    
                    Conversation(
                        userId = userId,
                        userName = user?.name ?: user?.phoneNumber ?: "$userId",
                        userAvatar = user?.avatarUrl,
                        lastMessage = latestMessage?.content,
                        lastMessageTime = latestMessage?.timestamp,
                        unreadCount = unreadCount,
                        isOnline = user?.isOnline ?: false
                    )
                }.sortedByDescending { it.lastMessageTime }
                
                _conversations.value = conversationsList
            }
        }
    }
    
    /**
     * Observes new messages from WebSocket to update conversations in real-time.
     * 
     * This method listens to incoming message events and updates the conversations
     * list accordingly. When a new message arrives, it either creates a new
     * conversation or updates an existing one with the latest message and timestamp.
     */
    private fun observeNewMessages() {
        viewModelScope.launch {
            webSocketManager.messageReceived.collect { messageEvent ->
                // Update conversation list when a new message is received
                val senderId = messageEvent.senderId
                val receiverId = messageEvent.receiverId
                
                // Determine the other user ID (not current user if this is from current user)
                val otherUserId = if (messageEvent.isFromCurrentUser) receiverId else senderId
                
                if (otherUserId != null) {
                    // Fetch user details from local database
                    var user = userDao.getUserById(otherUserId)
                    
                    // If user not in local DB, fetch from API
                    if (user == null) {
                        getUserByIdUseCase(otherUserId).fold(
                            onSuccess = { userDto ->
                                user = userDao.getUserById(otherUserId)
                            },
                            onFailure = { 
                                // API call failed, user remains null
                            }
                        )
                    }
                    
                    val userName = user?.name ?: user?.phoneNumber ?: "$otherUserId"
                    val userAvatar = user?.avatarUrl
                    
                    // Create or update conversation
                    val conversation = Conversation(
                        userId = otherUserId,
                        userName = userName,
                        userAvatar = userAvatar,
                        lastMessage = messageEvent.content,
                        lastMessageTime = messageEvent.timestamp,
                        unreadCount = if (!messageEvent.isFromCurrentUser) 1 else 0,
                        isOnline = user?.isOnline ?: false
                    )
                    
                    addOrUpdateConversation(conversation)
                }
            }
        }
    }
    
    fun addOrUpdateConversation(conversation: Conversation) {
        val currentList = _conversations.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.userId == conversation.userId }
        
        if (existingIndex != -1) {
            // Update existing conversation and move to top
            val existing = currentList[existingIndex]
            val updated = conversation.copy(
                unreadCount = existing.unreadCount + conversation.unreadCount
            )
            currentList.removeAt(existingIndex)
            currentList.add(0, updated)
        } else {
            // Add new conversation at the top
            currentList.add(0, conversation)
        }
        
        _conversations.value = currentList
    }
    
    fun addConversationIfNotExists(userId: Long, userName: String, userAvatar: String? = null) {
        val exists = _conversations.value.any { it.userId == userId }
        if (!exists) {
            val newConversation = Conversation(
                userId = userId,
                userName = userName,
                userAvatar = userAvatar,
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0,
                isOnline = false
            )
            addOrUpdateConversation(newConversation)
        }
    }
    
    fun removeConversation(userId: Long) {
        _conversations.value = _conversations.value.filter { it.userId != userId }
    }
    
    fun clearAllConversations() {
        _conversations.value = emptyList()
    }
    
    fun clearUnreadCount(userId: Long) {
        val currentList = _conversations.value.toMutableList()
        val index = currentList.indexOfFirst { it.userId == userId }
        
        if (index != -1) {
            currentList[index] = currentList[index].copy(unreadCount = 0)
            _conversations.value = currentList
        }
    }
}
