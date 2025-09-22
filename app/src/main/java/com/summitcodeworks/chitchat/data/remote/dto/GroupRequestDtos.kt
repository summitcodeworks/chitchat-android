package com.summitcodeworks.chitchat.data.remote.dto

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<Long> = emptyList()
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val avatarUrl: String? = null,
    val isPublic: Boolean? = null
)

data class AddMembersRequest(
    val userIds: List<Long>
)

data class UpdateMemberRoleRequest(
    val role: String // "ADMIN", "MODERATOR", "MEMBER"
)

data class InviteToGroupRequest(
    val phoneNumbers: List<String>,
    val message: String? = null
)
