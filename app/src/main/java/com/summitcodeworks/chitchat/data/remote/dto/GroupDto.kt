package com.summitcodeworks.chitchat.data.remote.dto

data class GroupDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val groupPicture: String? = null,
    val createdBy: Long,
    val createdAt: String,
    val isActive: Boolean = true,
    val members: List<GroupMemberDto>? = null
)

data class GroupMemberDto(
    val groupId: Long,
    val userId: Long,
    val role: String = "MEMBER", // ADMIN, MEMBER
    val joinedAt: String,
    val user: UserDto? = null
)

