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

/**
 * ViewModel for managing the home screen functionality in ChitChat.
 * 
 * This ViewModel handles the main home screen operations including:
 * - Loading and displaying the current user's profile information
 * - Managing online/offline status updates
 * - Handling loading states and error conditions
 * 
 * The home screen serves as the main entry point after authentication and
 * provides access to user profile information and status management.
 * 
 * Key responsibilities:
 * - Load current user profile from the server
 * - Update user's online/offline status
 * - Manage UI state for loading and error conditions
 * - Provide user data to home screen components
 * 
 * @param getUserProfileUseCase Use case for fetching current user profile
 * @param updateOnlineStatusUseCase Use case for updating user's online status
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateOnlineStatusUseCase: UpdateOnlineStatusUseCase
) : ViewModel() {
    
    // Current user profile information
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    // Loading state for profile operations
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state for handling failures
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Loads the current user's profile information from the server.
     * 
     * This method fetches the complete user profile including name, avatar,
     * about section, and other profile details. It manages loading states
     * and handles any errors that occur during the API call.
     */
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
    
    /**
     * Updates the user's online/offline status.
     * 
     * This method sends the user's online status to the server and updates
     * the local user state accordingly. It's typically called when the app
     * goes to foreground/background or when the user manually changes status.
     * 
     * @param isOnline True if user is online, false if offline
     */
    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            updateOnlineStatusUseCase(isOnline)
                .fold(
                    onSuccess = {
                        // Update local user state to reflect the new status
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
    
    /**
     * Clears any error state from the ViewModel.
     * 
     * This method resets the error state, typically called when the user
     * dismisses an error message or retries a failed operation.
     */
    fun clearError() {
        _error.value = null
    }
}
