package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Call
import com.summitcodeworks.chitchat.domain.model.CallType
import com.summitcodeworks.chitchat.domain.usecase.call.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallsViewModel @Inject constructor(
    private val initiateCallUseCase: InitiateCallUseCase,
    private val answerCallUseCase: AnswerCallUseCase,
    private val rejectCallUseCase: RejectCallUseCase,
    private val endCallUseCase: EndCallUseCase,
    private val getCallHistoryUseCase: GetCallHistoryUseCase
) : ViewModel() {
    
    private val _calls = MutableStateFlow<List<Call>>(emptyList())
    val calls: StateFlow<List<Call>> = _calls.asStateFlow()
    
    private val _currentCall = MutableStateFlow<Call?>(null)
    val currentCall: StateFlow<Call?> = _currentCall.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun initiateCall(token: String, calleeId: Long? = null, groupId: Long? = null, callType: CallType) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            initiateCallUseCase(token, calleeId, groupId, callType)
                .fold(
                    onSuccess = { call ->
                        _currentCall.value = call
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun answerCall(token: String, sessionId: String, accepted: Boolean, sdpAnswer: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            answerCallUseCase(token, sessionId, accepted, sdpAnswer)
                .fold(
                    onSuccess = { call ->
                        _currentCall.value = call
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun rejectCall(token: String, sessionId: String, reason: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            rejectCallUseCase(token, sessionId, reason)
                .fold(
                    onSuccess = { call ->
                        _currentCall.value = call
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun endCall(token: String, sessionId: String, reason: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            endCallUseCase(token, sessionId, reason)
                .fold(
                    onSuccess = { call ->
                        _currentCall.value = call
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadCallHistory(token: String, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getCallHistoryUseCase(token, page, size)
                .fold(
                    onSuccess = { calls ->
                        _calls.value = calls
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun observeLocalCallHistory(userId: Long) {
        viewModelScope.launch {
            getCallHistoryUseCase.getLocalCallHistory(userId)
                .collect { calls ->
                    _calls.value = calls
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearCurrentCall() {
        _currentCall.value = null
    }
}
