package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Message
import com.summitcodeworks.chitchat.domain.model.MessageType
import com.summitcodeworks.chitchat.domain.usecase.message.GetConversationMessagesUseCase
import com.summitcodeworks.chitchat.domain.usecase.message.SendMessageUseCase
import com.summitcodeworks.chitchat.presentation.state.ChatState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getConversationMessagesUseCase: GetConversationMessagesUseCase
) : ViewModel() {
    
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
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
}
