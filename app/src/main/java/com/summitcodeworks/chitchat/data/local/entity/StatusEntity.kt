package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "statuses",
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
        Index(value = ["statusType"])
    ]
)
data class StatusEntity(
    @PrimaryKey
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
    val reactionCount: Int = 0
)
