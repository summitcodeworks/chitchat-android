package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.domain.usecase.user.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val syncContactsUseCase: SyncContactsUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val unblockUserUseCase: UnblockUserUseCase,
    private val updateOnlineStatusUseCase: UpdateOnlineStatusUseCase
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts: StateFlow<List<User>> = _contacts.asStateFlow()
    
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
    
    fun updateUserProfile(
        name: String,
        about: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            updateUserProfileUseCase(name, about)
                .fold(
                    onSuccess = { updatedUser ->
                        _user.value = updatedUser
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun syncContacts(contacts: List<SyncContact>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Convert ViewModel Contact to UseCase Contact
            val useCaseContacts = contacts.map { contact ->
                com.summitcodeworks.chitchat.domain.usecase.user.Contact(
                    phoneNumber = contact.phoneNumber,
                    displayName = contact.displayName
                )
            }
            
            syncContactsUseCase(useCaseContacts)
                .fold(
                    onSuccess = { syncedUsers ->
                        // Store synced users directly
                        _contacts.value = syncedUsers
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun blockUser(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            blockUserUseCase(userId)
                .fold(
                    onSuccess = {
                        // Update local contacts to mark user as blocked
                        _contacts.value = _contacts.value.map { contact ->
                            if (contact.id == userId) {
                                contact.copy(isBlocked = true)
                            } else {
                                contact
                            }
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
    
    fun unblockUser(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            unblockUserUseCase(userId)
                .fold(
                    onSuccess = {
                        // Update local contacts to mark user as unblocked
                        _contacts.value = _contacts.value.map { contact ->
                            if (contact.id == userId) {
                                contact.copy(isBlocked = false)
                            } else {
                                contact
                            }
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

data class SyncContact(
    val phoneNumber: String,
    val displayName: String
)
