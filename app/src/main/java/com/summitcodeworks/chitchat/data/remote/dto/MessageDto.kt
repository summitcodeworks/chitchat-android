package com.summitcodeworks.chitchat.data.remote.dto

data class MessageDto(
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
    val deleteForEveryone: Boolean = false,
    val sender: UserDto? = null,
    val media: MediaDto? = null
)

data class SendMessageRequest(
    val receiverId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    val messageType: String = "TEXT",
    val replyToMessageId: String? = null,
    val mediaId: Long? = null
)

data class MessagePageResponse(
    val content: List<MessageDto>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)

data class Pageable(
    val sort: Sort,
    val offset: Int,
    val pageSize: Int,
    val pageNumber: Int,
    val paged: Boolean,
    val unpaged: Boolean
)

data class Sort(
    val sorted: Boolean,
    val unsorted: Boolean,
    val empty: Boolean
)

data class DeleteMessageRequest(
    val deleteForEveryone: Boolean = false
)

data class MarkMessageReadRequest(
    val messageId: String
)
