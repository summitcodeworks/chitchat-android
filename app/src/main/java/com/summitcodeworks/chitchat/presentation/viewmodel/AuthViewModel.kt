package com.summitcodeworks.chitchat.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.domain.usecase.auth.SendOtpUseCase
import com.summitcodeworks.chitchat.domain.usecase.auth.SignInWithPhoneUseCase
import com.summitcodeworks.chitchat.presentation.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase,
    private val signInWithPhoneUseCase: SignInWithPhoneUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var _verificationId: String? = null
    
    init {
        observeCurrentUser()
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification completed
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    codeSent = true
                )
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Verification failed"
                )
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, token)
                _verificationId = verificationId
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    codeSent = true
                )
            }
        }

        sendOtpUseCase(phoneNumber, activity, callbacks)
    }

    fun signInWithPhone(phoneNumber: String, code: String) {
        val verificationId = _verificationId
        if (verificationId == null) {
            _authState.value = _authState.value.copy(error = "No verification ID found")
            return
        }

        signInWithPhone(phoneNumber, verificationId, code)
    }

    private fun signInWithPhone(phoneNumber: String, verificationId: String, code: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            
            signInWithPhoneUseCase(phoneNumber, verificationId, code)
                .fold(
                    onSuccess = { token ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            token = token
                        )
                    },
                    onFailure = { exception ->
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                )
        }
    }
    
    private fun observeCurrentUser() {
        viewModelScope.launch {
            signInWithPhoneUseCase.observeCurrentUser().collect { user ->
                _currentUser.value = user
                if (user == null) {
                    _authState.value = _authState.value.copy(isAuthenticated = false)
                }
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null, codeSent = false)
    }
}
