package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["uploadedBy"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uploadedBy"]),
        Index(value = ["mediaType"]),
        Index(value = ["uploadedAt"])
    ]
)
data class MediaEntity(
    @PrimaryKey
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
    val duration: Long? = null, // for video/audio in seconds
    val messageId: String? = null // for messages that contain media
)
