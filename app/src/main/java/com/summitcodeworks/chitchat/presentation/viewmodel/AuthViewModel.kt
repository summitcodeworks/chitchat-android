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
import com.summitcodeworks.chitchat.data.auth.AuthTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase,
    private val signInWithPhoneUseCase: SignInWithPhoneUseCase,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var _verificationId: String? = null
    
    init {
        checkAuthenticationStatus()
        observeCurrentUser()
        observeTokenManager()
    }

    private fun observeTokenManager() {
        viewModelScope.launch {
            authTokenManager.currentToken.collect { token ->
                _authState.value = _authState.value.copy(
                    token = token,
                    isAuthenticated = token != null
                )
            }
        }
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)

            try {
                // Check if user is already signed in with Firebase
                val firebaseUser = signInWithPhoneUseCase.getCurrentFirebaseUser()
                if (firebaseUser != null) {
                    // User is authenticated with Firebase
                    _authState.value = _authState.value.copy(
                        isAuthenticated = true,
                        token = "authenticated", // We can get actual token later if needed
                        isLoading = false
                    )
                } else {
                    // No Firebase user found
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Error checking auth state
                _authState.value = _authState.value.copy(
                    isAuthenticated = false,
                    isLoading = false,
                    error = e.message
                )
            }
        }
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
                        authTokenManager.setToken(token)
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
            // Observe Firebase auth state changes
            signInWithPhoneUseCase.observeAuthState().collect { firebaseUser ->
                if (firebaseUser != null) {
                    _authState.value = _authState.value.copy(isAuthenticated = true)
                } else {
                    _authState.value = _authState.value.copy(
                        isAuthenticated = false,
                        token = null
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null, codeSent = false)
    }

    fun signOut() {
        viewModelScope.launch {
            signInWithPhoneUseCase.signOut()
            authTokenManager.clearToken()
            _authState.value = AuthState() // Reset to default state
            _currentUser.value = null
        }
    }
}
