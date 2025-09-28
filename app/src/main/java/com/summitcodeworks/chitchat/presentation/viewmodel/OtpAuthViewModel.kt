package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.data.remote.dto.OtpAuthResponse
import com.summitcodeworks.chitchat.domain.usecase.auth.SendOtpSmsUseCase
import com.summitcodeworks.chitchat.domain.usecase.auth.VerifyOtpSmsUseCase
import com.summitcodeworks.chitchat.domain.usecase.notification.UpdateDeviceTokenUseCase
import com.summitcodeworks.chitchat.presentation.state.AuthState
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * OTP Authentication ViewModel
 * 
 * Handles SMS-based OTP authentication as the primary authentication method
 * for the ChitChat application.
 */
@HiltViewModel
class OtpAuthViewModel @Inject constructor(
    private val sendOtpSmsUseCase: SendOtpSmsUseCase,
    private val verifyOtpSmsUseCase: VerifyOtpSmsUseCase,
    private val updateDeviceTokenUseCase: UpdateDeviceTokenUseCase,
    private val otpAuthManager: OtpAuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "OtpAuthViewModel"
    }
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()
    
    private val _authResponse = MutableStateFlow<OtpAuthResponse?>(null)
    val authResponse: StateFlow<OtpAuthResponse?> = _authResponse.asStateFlow()

    init {
        checkAuthenticationStatus()
        observeAuthState()
    }

    /**
     * Sends OTP to the specified phone number
     */
    fun sendOtp(phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            sendOtpSmsUseCase(phoneNumber)
                .fold(
                    onSuccess = {
                        _otpSent.value = true
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            codeSent = true
                        )
                    },
                    onFailure = { exception ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to send OTP"
                        )
                    }
                )
        }
    }

    /**
     * Verifies OTP and authenticates the user
     */
    fun verifyOtp(phoneNumber: String, otp: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            verifyOtpSmsUseCase(phoneNumber, otp)
                .fold(
                    onSuccess = { authResponse ->
                        _authResponse.value = authResponse
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            token = authResponse.accessToken
                        )

                        // Update device token after successful authentication
                        // Use the fresh token from the response to avoid any persistence timing issues
                        updateDeviceTokenAfterAuth(authResponse.accessToken)
                    },
                    onFailure = { exception ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "OTP verification failed"
                        )
                    }
                )
        }
    }

    /**
     * Updates device token after successful authentication
     * This runs in background and doesn't affect user experience if it fails
     */
    private fun updateDeviceTokenAfterAuth(token: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating device token after successful OTP verification...")

                val result = updateDeviceTokenUseCase(token)

                result.fold(
                    onSuccess = { user ->
                        Log.d(TAG, "Device token updated successfully after authentication for user: ${user.name}")
                    },
                    onFailure = { exception ->
                        Log.w(TAG, "Failed to update device token after authentication: ${exception.message}", exception)
                        // Don't show error to user, this is a background operation
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception while updating device token after authentication", e)
            }
        }
    }

    /**
     * Resends OTP to the same phone number
     */
    fun resendOtp(phoneNumber: String) {
        sendOtp(phoneNumber)
    }

    /**
     * Clears error state
     */
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    /**
     * Resets OTP sent state (for retry)
     */
    fun resetOtpState() {
        _otpSent.value = false
        _authState.value = _authState.value.copy(codeSent = false)
    }

    /**
     * Signs out the current user
     */
    fun signOut() {
        viewModelScope.launch {
            otpAuthManager.signOut()
            _authState.value = AuthState() // Reset to default state
            _otpSent.value = false
            _authResponse.value = null
        }
    }

    /**
     * Checks the current authentication status
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            try {
                if (otpAuthManager.isUserAuthenticated()) {
                    val token = otpAuthManager.getCurrentToken()
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        token = token,
                        isLoading = false
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isAuthenticated = false,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Observes authentication state changes
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            otpAuthManager.isAuthenticated.collect { isAuthenticated ->
                if (!isAuthenticated) {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        token = null
                    )
                    _otpSent.value = false
                    _authResponse.value = null
                }
            }
        }
    }
}
