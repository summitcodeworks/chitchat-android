package com.summitcodeworks.chitchat.presentation.state

import com.summitcodeworks.chitchat.domain.model.Group

data class GroupsState(
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
