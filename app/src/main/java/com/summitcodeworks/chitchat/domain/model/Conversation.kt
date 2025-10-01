package com.summitcodeworks.chitchat.domain.model

data class Conversation(
    val userId: Long,
    val userName: String,
    val userAvatar: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)
