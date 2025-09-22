package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
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
