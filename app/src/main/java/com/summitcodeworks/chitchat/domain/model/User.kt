package com.summitcodeworks.chitchat.domain.model

data class User(
    val id: Long,
    val phoneNumber: String,
    val name: String,
    val avatarUrl: String? = null,
    val about: String? = null,
    val lastSeen: String? = null,
    val isOnline: Boolean = false,
    val createdAt: String? = null,
    val isBlocked: Boolean = false,
    val isContact: Boolean = false
)
