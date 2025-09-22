package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.Media

data class MediaState(
    val userMedia: List<Media> = emptyList(),
    val selectedMedia: Media? = null,
    val isLoading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null
)
