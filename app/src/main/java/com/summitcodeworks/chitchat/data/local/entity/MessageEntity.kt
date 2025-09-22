package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiverId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["senderId"]),
        Index(value = ["receiverId"]),
        Index(value = ["timestamp"]),
        Index(value = ["messageType"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: Long,
    val receiverId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    val messageType: String, // TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT
    val timestamp: String,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val replyToMessageId: String? = null,
    val mediaId: Long? = null,
    val isDeleted: Boolean = false,
    val deleteForEveryone: Boolean = false
)
