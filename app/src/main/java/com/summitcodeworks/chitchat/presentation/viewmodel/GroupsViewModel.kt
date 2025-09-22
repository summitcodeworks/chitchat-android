package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Group
import com.summitcodeworks.chitchat.domain.usecase.group.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val createGroupUseCase: CreateGroupUseCase,
    private val getUserGroupsUseCase: GetUserGroupsUseCase,
    private val getGroupDetailsUseCase: GetGroupDetailsUseCase,
    private val updateGroupUseCase: UpdateGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val addGroupMembersUseCase: AddGroupMembersUseCase,
    private val removeGroupMemberUseCase: RemoveGroupMemberUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase
) : ViewModel() {
    
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val groups: StateFlow<List<Group>> = _groups.asStateFlow()
    
    private val _selectedGroup = MutableStateFlow<Group?>(null)
    val selectedGroup: StateFlow<Group?> = _selectedGroup.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun createGroup(
        token: String,
        name: String,
        description: String? = null,
        isPublic: Boolean = false,
        memberIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            createGroupUseCase(token, name, description, isPublic, memberIds)
                .fold(
                    onSuccess = { group ->
                        _groups.value = _groups.value + group
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadUserGroups(token: String, page: Int = 0, limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getUserGroupsUseCase(token, page, limit)
                .fold(
                    onSuccess = { groups ->
                        _groups.value = groups
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadGroupDetails(token: String, groupId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getGroupDetailsUseCase(token, groupId)
                .fold(
                    onSuccess = { group ->
                        _selectedGroup.value = group
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun updateGroup(
        token: String,
        groupId: Long,
        name: String? = null,
        description: String? = null,
        isPublic: Boolean? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            updateGroupUseCase(token, groupId, name, description, isPublic)
                .fold(
                    onSuccess = { group ->
                        _selectedGroup.value = group
                        _groups.value = _groups.value.map { if (it.id == groupId) group else it }
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun deleteGroup(token: String, groupId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            deleteGroupUseCase(token, groupId)
                .fold(
                    onSuccess = {
                        _groups.value = _groups.value.filter { it.id != groupId }
                        if (_selectedGroup.value?.id == groupId) {
                            _selectedGroup.value = null
                        }
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun addGroupMembers(token: String, groupId: Long, memberIds: List<Long>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            addGroupMembersUseCase(token, groupId, memberIds)
                .fold(
                    onSuccess = { groups ->
                        // Update the selected group with new members
                        loadGroupDetails(token, groupId)
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun removeGroupMember(token: String, groupId: Long, memberId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            removeGroupMemberUseCase(token, groupId, memberId)
                .fold(
                    onSuccess = {
                        // Reload group details to get updated member list
                        loadGroupDetails(token, groupId)
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun leaveGroup(token: String, groupId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            leaveGroupUseCase(token, groupId)
                .fold(
                    onSuccess = {
                        _groups.value = _groups.value.filter { it.id != groupId }
                        if (_selectedGroup.value?.id == groupId) {
                            _selectedGroup.value = null
                        }
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun observeUserGroups() {
        viewModelScope.launch {
            getUserGroupsUseCase.getUserGroupsFlow()
                .collect { groups ->
                    _groups.value = groups
                }
        }
    }
    
    fun selectGroup(group: Group) {
        _selectedGroup.value = group
    }
    
    fun clearSelectedGroup() {
        _selectedGroup.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}
