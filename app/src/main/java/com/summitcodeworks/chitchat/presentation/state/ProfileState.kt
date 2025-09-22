package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.User

data class ProfileState(
    val isLoading: Boolean = false,
    val existingProfile: User? = null,
    val isProfileComplete: Boolean = false,
    val error: String? = null
)