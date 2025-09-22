package com.summitcodeworks.chitchat.data.remote.dto

data class MediaDto(
    val id: Long,
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val mediaType: String, // IMAGE, VIDEO, AUDIO, DOCUMENT
    val mimeType: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val uploadedBy: Long,
    val uploadedAt: String,
    val duration: Long? = null // for video/audio in seconds
)

data class UploadMediaResponse(
    val mediaId: Long,
    val url: String,
    val thumbnailUrl: String? = null
)

data class MediaPageResponse(
    val content: List<MediaDto>,
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
