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
 * ViewModel for handling OTP (One-Time Password) authentication in ChitChat.
 * 
 * This ViewModel manages the complete OTP authentication flow including:
 * - Sending OTP via SMS to user's phone number
 * - Verifying OTP codes entered by the user
 * - Managing authentication state and token storage
 * - Updating device tokens for push notifications
 * - Handling authentication persistence and sign-out
 * 
 * The OTP authentication system provides secure, phone-number-based authentication
 * without requiring traditional username/password credentials. It integrates with
 * Firebase for SMS delivery and maintains session state across app restarts.
 * 
 * Key features:
 * - SMS-based OTP sending and verification
 * - Automatic device token registration after authentication
 * - Persistent authentication state management
 * - Comprehensive error handling and user feedback
 * - Session management and sign-out functionality
 * 
 * @param sendOtpSmsUseCase Use case for sending OTP via SMS
 * @param verifyOtpSmsUseCase Use case for verifying OTP codes
 * @param updateDeviceTokenUseCase Use case for updating device tokens
 * @param otpAuthManager Authentication manager for session handling
 * 
 * @author ChitChat Development Team
 * @since 1.0
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
    
    // Main authentication state containing token, loading status, and errors
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Flag indicating whether OTP has been successfully sent to user
    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()
    
    // Complete authentication response from server after successful verification
    private val _authResponse = MutableStateFlow<OtpAuthResponse?>(null)
    val authResponse: StateFlow<OtpAuthResponse?> = _authResponse.asStateFlow()

    init {
        // Initialize authentication state and observers
        checkAuthenticationStatus()
        observeAuthState()
    }

    /**
     * Sends OTP to the specified phone number via SMS.
     * 
     * This method initiates the OTP authentication process by sending a one-time
     * password to the user's phone number. The phone number should be in international
     * format (e.g., "+1234567890").
     * 
     * @param phoneNumber The phone number in international format to send OTP to
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
     * Verifies the OTP code and authenticates the user.
     * 
     * This method validates the OTP code entered by the user and completes
     * the authentication process. Upon successful verification, it stores
     * the authentication token and updates the device token for push notifications.
     * 
     * @param phoneNumber The phone number used for OTP (should match the one used for sending)
     * @param otp The OTP code entered by the user
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
     * Updates the device token for push notifications after successful authentication.
     * 
     * This method runs in the background and registers the device token with the
     * server to enable push notifications. It doesn't affect the user experience
     * if it fails, as it's a background operation.
     * 
     * @param token The authentication token to use for the device token update request
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
