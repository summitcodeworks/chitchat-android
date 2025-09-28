package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.domain.usecase.notification.UpdateDeviceTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for SplashScreen with authentication and device token management
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    val otpAuthManager: OtpAuthManager,
    private val updateDeviceTokenUseCase: UpdateDeviceTokenUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SplashViewModel"
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