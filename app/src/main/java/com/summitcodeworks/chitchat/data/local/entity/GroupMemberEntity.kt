package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["groupId", "userId"],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["userId"])
    ]
)
data class GroupMemberEntity(
    val groupId: Long,
    val userId: Long,
    val role: String = "MEMBER", // ADMIN, MEMBER
    val joinedAt: String
)
