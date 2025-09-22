package com.summitcodeworks.chitchat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String? = null,
    val groupPicture: String? = null,
    val createdBy: Long,
    val createdAt: String,
    val isActive: Boolean = true
)
