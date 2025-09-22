package com.summitcodeworks.chitchat.domain.model

data class Group(
    val id: Long,
    val name: String,
    val description: String? = null,
    val groupPicture: String? = null,
    val createdBy: Long,
    val createdAt: String,
    val isActive: Boolean = true,
    val members: List<GroupMember>? = null
)

data class GroupMember(
    val groupId: Long,
    val userId: Long,
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAt: String,
    val user: User? = null
)

enum class GroupRole {
    ADMIN, MEMBER
}
