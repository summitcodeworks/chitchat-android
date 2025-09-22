package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calls",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["callerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["calleeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["callerId"]),
        Index(value = ["calleeId"]),
        Index(value = ["startTime"])
    ]
)
data class CallEntity(
    @PrimaryKey
    val sessionId: String,
    val callerId: Long,
    val calleeId: Long,
    val callType: String, // VOICE, VIDEO
    val status: String, // INITIATED, RINGING, ANSWERED, REJECTED, ENDED, MISSED
    val startTime: String,
    val endTime: String? = null,
    val duration: Long? = null, // in seconds
    val groupId: Long? = null
)
