package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.Message

data class ChatState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val error: String? = null
)
