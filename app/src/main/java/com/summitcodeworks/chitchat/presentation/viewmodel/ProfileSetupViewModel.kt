package com.summitcodeworks.chitchat.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.usecase.user.GetUserProfileUseCase
import com.summitcodeworks.chitchat.domain.usecase.user.UpdateUserProfileUseCase
import com.summitcodeworks.chitchat.presentation.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    init {
        loadExistingProfile()
    }

    private fun loadExistingProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)

            getUserProfileUseCase()
                .fold(
                    onSuccess = { user ->
                        // Load profile data but don't auto-navigate
                        // User needs to explicitly save to navigate to home
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            existingProfile = user,
                            isProfileComplete = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        // Don't show error for new users without profile
                        // Just set loading to false and let them create profile
                        _profileState.value = _profileState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                )
        }
    }

    fun saveProfile(name: String, bio: String?, avatarUri: Uri?) {
        if (name.isBlank()) {
            _profileState.value = _profileState.value.copy(error = "Name is required")
            return
        }

        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, error = null)

            updateUserProfileUseCase(
                name = name,
                bio = bio,
                avatarUri = avatarUri
            ).fold(
                onSuccess = { updatedUser ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        existingProfile = updatedUser,
                        isProfileComplete = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _profileState.value = _profileState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to update profile"
                    )
                }
            )
        }
    }

    fun clearError() {
        _profileState.value = _profileState.value.copy(error = null)
    }
}