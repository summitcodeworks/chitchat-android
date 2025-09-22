package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.User

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
