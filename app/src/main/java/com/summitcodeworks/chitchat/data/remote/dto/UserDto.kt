package com.summitcodeworks.chitchat.data.remote.dto

data class UserDto(
    val id: Long,
    val phoneNumber: String,
    val name: String,
    val avatarUrl: String? = null,
    val about: String? = null,
    val lastSeen: String? = null,
    val isOnline: Boolean = false,
    val createdAt: String? = null
)

data class FirebaseAuthRequest(
    val idToken: String,
    val name: String? = null,
    val deviceInfo: String? = null
)

data class FirebaseAuthResponse(
    val token: String,
    val user: UserDto,
    val isNewUser: Boolean
)

data class UpdateProfileRequest(
    val name: String,
    val avatarUrl: String? = null,
    val about: String? = null
)

data class ContactSyncRequest(
    val contacts: List<ContactDto>
)

data class ContactDto(
    val phoneNumber: String,
    val displayName: String
)

data class BlockUserRequest(
    val userId: Long,
    val reason: String? = null
)

data class UpdateStatusRequest(
    val isOnline: Boolean
)
