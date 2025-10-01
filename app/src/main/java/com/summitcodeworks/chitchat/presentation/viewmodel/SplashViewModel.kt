package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.domain.usecase.notification.UpdateDeviceTokenUseCase
import com.summitcodeworks.chitchat.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for SplashScreen with authentication and device token management
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    val otpAuthManager: OtpAuthManager,
    private val updateDeviceTokenUseCase: UpdateDeviceTokenUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SplashViewModel"
    }

    private val _hasCompleteProfile = MutableStateFlow<Boolean?>(null)
    val hasCompleteProfile: StateFlow<Boolean?> = _hasCompleteProfile.asStateFlow()

    /**
     * Checks if user has a complete profile
     */
    fun checkProfileCompleteness() {
        viewModelScope.launch {
            try {
                getUserProfileUseCase()
                    .fold(
                        onSuccess = { user ->
                            _hasCompleteProfile.value = user.name.isNotBlank()
                        },
                        onFailure = {
                            _hasCompleteProfile.value = false
                        }
                    )
            } catch (e: Exception) {
                _hasCompleteProfile.value = false
            }
        }
    }

    /**
     * Updates device token on app launch if user is authenticated
     */
    fun updateDeviceTokenIfAuthenticated() {
        val currentToken = otpAuthManager.currentToken.value
        val isAuthenticated = otpAuthManager.isAuthenticated.value

        if (isAuthenticated && !currentToken.isNullOrBlank()) {
            viewModelScope.launch {
                try {
                    Log.d(TAG, "Updating device token on app launch...")

                    val result = updateDeviceTokenUseCase(currentToken)

                    result.fold(
                        onSuccess = { user ->
                            Log.d(TAG, "Device token updated successfully for user: ${user.name}")
                        },
                        onFailure = { exception ->
                            Log.w(TAG, "Failed to update device token: ${exception.message}", exception)
                            // Don't block the user experience for token update failures
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while updating device token", e)
                }
            }
        } else {
            Log.d(TAG, "User not authenticated, skipping device token update")
        }
    }
}