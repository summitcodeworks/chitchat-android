package com.summitcodeworks.chitchat.data.remote.dto

data class StatusDto(
    val id: Long,
    val userId: Long,
    val content: String,
    val mediaId: Long? = null,
    val statusType: String, // TEXT, IMAGE, VIDEO
    val backgroundColor: String? = null,
    val font: String? = null,
    val privacy: String = "CONTACTS", // PUBLIC, CONTACTS, SELECTED
    val createdAt: String,
    val expiresAt: String,
    val viewCount: Int = 0,
    val reactionCount: Int = 0,
    val user: UserDto? = null,
    val media: MediaDto? = null
)

data class CreateStatusRequest(
    val content: String,
    val mediaId: Long? = null,
    val statusType: String, // TEXT, IMAGE, VIDEO
    val backgroundColor: String? = null,
    val font: String? = null,
    val privacy: String = "CONTACTS"
)

data class ViewStatusRequest(
    val statusId: Long
)

data class ReactToStatusRequest(
    val reaction: String // LIKE, LOVE, LAUGH, SAD, ANGRY
)

data class StatusViewDto(
    val id: Long,
    val statusId: Long,
    val userId: Long,
    val viewedAt: String,
    val user: UserDto? = null
)
