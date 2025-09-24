package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.domain.usecase.user.GetUserProfileUseCase
import com.summitcodeworks.chitchat.domain.usecase.user.UpdateOnlineStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateOnlineStatusUseCase: UpdateOnlineStatusUseCase
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            getUserProfileUseCase()
                .fold(
                    onSuccess = { user ->
                        _user.value = user
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            updateOnlineStatusUseCase(isOnline)
                .fold(
                    onSuccess = {
                        // Update local user state
                        _user.value?.let { currentUser ->
                            _user.value = currentUser.copy(isOnline = isOnline)
                        }
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
