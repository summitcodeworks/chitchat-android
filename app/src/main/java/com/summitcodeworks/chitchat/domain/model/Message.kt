package com.summitcodeworks.chitchat.domain.model

data class Message(
    val id: String,
    val senderId: Long,
    val receiverId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    val messageType: MessageType,
    val timestamp: String,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val replyToMessageId: String? = null,
    val mediaId: Long? = null,
    val isDeleted: Boolean = false,
    val deleteForEveryone: Boolean = false,
    val sender: User? = null,
    val media: Media? = null
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT
}
