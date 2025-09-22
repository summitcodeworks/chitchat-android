package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Status
import com.summitcodeworks.chitchat.domain.model.StatusView
import com.summitcodeworks.chitchat.domain.usecase.status.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val createStatusUseCase: CreateStatusUseCase,
    private val getUserStatusesUseCase: GetUserStatusesUseCase,
    private val getActiveStatusesUseCase: GetActiveStatusesUseCase,
    private val getContactsStatusesUseCase: GetContactsStatusesUseCase,
    private val viewStatusUseCase: ViewStatusUseCase,
    private val reactToStatusUseCase: ReactToStatusUseCase,
    private val deleteStatusUseCase: DeleteStatusUseCase,
    private val getStatusViewsUseCase: GetStatusViewsUseCase
) : ViewModel() {
    
    private val _userStatuses = MutableStateFlow<List<Status>>(emptyList())
    val userStatuses: StateFlow<List<Status>> = _userStatuses.asStateFlow()
    
    private val _activeStatuses = MutableStateFlow<List<Status>>(emptyList())
    val activeStatuses: StateFlow<List<Status>> = _activeStatuses.asStateFlow()
    
    private val _contactsStatuses = MutableStateFlow<List<Status>>(emptyList())
    val contactsStatuses: StateFlow<List<Status>> = _contactsStatuses.asStateFlow()
    
    private val _statusViews = MutableStateFlow<List<StatusView>>(emptyList())
    val statusViews: StateFlow<List<StatusView>> = _statusViews.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun createStatus(
        token: String,
        content: String,
        mediaId: Long? = null,
        statusType: String = "TEXT",
        backgroundColor: String? = null,
        font: String? = null,
        privacy: String = "CONTACTS"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            createStatusUseCase(token, content, mediaId, statusType, backgroundColor, font, privacy)
                .fold(
                    onSuccess = { status ->
                        _userStatuses.value = _userStatuses.value + status
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadUserStatuses(token: String, userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getUserStatusesUseCase(token, userId)
                .fold(
                    onSuccess = { statuses ->
                        _userStatuses.value = statuses
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadActiveStatuses(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getActiveStatusesUseCase(token)
                .fold(
                    onSuccess = { statuses ->
                        _activeStatuses.value = statuses
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadContactsStatuses(token: String, contactIds: List<Long>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getContactsStatusesUseCase(token, contactIds)
                .fold(
                    onSuccess = { statuses ->
                        _contactsStatuses.value = statuses
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun viewStatus(token: String, statusId: Long) {
        viewModelScope.launch {
            viewStatusUseCase(token, statusId)
                .fold(
                    onSuccess = {
                        // Status viewed successfully
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
        }
    }
    
    fun reactToStatus(token: String, statusId: Long, reaction: String) {
        viewModelScope.launch {
            reactToStatusUseCase(token, statusId, reaction)
                .fold(
                    onSuccess = {
                        // Reaction added successfully
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
        }
    }
    
    fun deleteStatus(token: String, statusId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            deleteStatusUseCase(token, statusId)
                .fold(
                    onSuccess = {
                        _userStatuses.value = _userStatuses.value.filter { it.id != statusId }
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadStatusViews(token: String, statusId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getStatusViewsUseCase(token, statusId)
                .fold(
                    onSuccess = { views ->
                        _statusViews.value = views
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun observeUserStatuses(userId: Long) {
        viewModelScope.launch {
            getUserStatusesUseCase.getUserStatusesFlow(userId)
                .collect { statuses ->
                    _userStatuses.value = statuses
                }
        }
    }
    
    fun observeActiveStatuses() {
        viewModelScope.launch {
            getActiveStatusesUseCase.getActiveStatusesFlow()
                .collect { statuses ->
                    _activeStatuses.value = statuses
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
