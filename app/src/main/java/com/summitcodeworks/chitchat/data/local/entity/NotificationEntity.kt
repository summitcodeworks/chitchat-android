package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["isRead"])
    ]
)
data class NotificationEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val title: String,
    val body: String,
    val type: String, // MESSAGE, CALL, STATUS, GROUP_UPDATE
    val data: String? = null, // JSON string for additional data
    val createdAt: String,
    val readAt: String? = null,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false
)
