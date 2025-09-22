package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.User

data class UserProfileState(
    val user: User? = null,
    val contacts: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
