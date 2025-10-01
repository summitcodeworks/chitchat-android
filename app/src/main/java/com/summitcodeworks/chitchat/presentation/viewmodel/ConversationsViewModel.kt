package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.summitcodeworks.chitchat.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor() : ViewModel() {
    
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    fun addOrUpdateConversation(conversation: Conversation) {
        val currentList = _conversations.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.userId == conversation.userId }
        
        if (existingIndex != -1) {
            // Update existing conversation and move to top
            currentList.removeAt(existingIndex)
            currentList.add(0, conversation)
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
}
