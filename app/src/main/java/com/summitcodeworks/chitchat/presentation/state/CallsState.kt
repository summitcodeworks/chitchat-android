package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.Call

data class CallsState(
    val calls: List<Call> = emptyList(),
    val currentCall: Call? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
