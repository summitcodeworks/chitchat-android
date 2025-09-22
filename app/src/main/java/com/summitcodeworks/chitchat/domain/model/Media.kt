package com.summitcodeworks.chitchat.domain.model

data class Media(
    val id: Long,
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val mediaType: MediaType,
    val mimeType: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val uploadedBy: Long,
    val uploadedAt: String,
    val duration: Long? = null // for video/audio in seconds
)

enum class MediaType {
    IMAGE, VIDEO, AUDIO, DOCUMENT
}
