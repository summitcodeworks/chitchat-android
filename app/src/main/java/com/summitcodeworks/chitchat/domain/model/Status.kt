package com.summitcodeworks.chitchat.domain.model

data class Status(
    val id: Long,
    val userId: Long,
    val content: String,
    val mediaId: Long? = null,
    val statusType: StatusType,
    val backgroundColor: String? = null,
    val font: String? = null,
    val privacy: Privacy = Privacy.CONTACTS,
    val createdAt: String,
    val expiresAt: String,
    val viewCount: Int = 0,
    val reactionCount: Int = 0,
    val user: User? = null,
    val media: Media? = null
)

enum class StatusType {
    TEXT, IMAGE, VIDEO
}

enum class Privacy {
    PUBLIC, CONTACTS, SELECTED
}

data class StatusView(
    val id: Long,
    val statusId: Long,
    val userId: Long,
    val viewedAt: String,
    val user: User? = null
)
