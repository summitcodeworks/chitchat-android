package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.Notification

data class NotificationsState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
