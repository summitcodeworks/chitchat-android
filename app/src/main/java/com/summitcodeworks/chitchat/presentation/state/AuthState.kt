package com.summitcodeworks.chitchat.presentation.state

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val token: String? = null,
    val error: String? = null,
    val codeSent: Boolean = false
)
