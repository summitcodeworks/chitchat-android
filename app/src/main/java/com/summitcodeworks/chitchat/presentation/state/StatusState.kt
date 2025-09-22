package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.Status
import com.summitcodeworks.chitchat.domain.model.StatusView

data class StatusState(
    val userStatuses: List<Status> = emptyList(),
    val activeStatuses: List<Status> = emptyList(),
    val contactsStatuses: List<Status> = emptyList(),
    val statusViews: List<StatusView> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
