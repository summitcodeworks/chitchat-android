package com.summitcodeworks.chitchat.domain.model

data class Notification(
    val id: Long,
    val userId: Long,
    val type: String,
    val title: String,
    val body: String,
    val data: String? = null,
    val isRead: Boolean = false,
    val createdAt: String,
    val readAt: String? = null
)
