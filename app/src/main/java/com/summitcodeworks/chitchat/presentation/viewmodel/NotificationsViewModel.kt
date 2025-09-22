package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Notification
import com.summitcodeworks.chitchat.domain.usecase.notification.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val registerDeviceUseCase: RegisterDeviceUseCase
) : ViewModel() {
    
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadNotifications(
        token: String,
        page: Int = 0,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getNotificationsUseCase(token, page, limit, unreadOnly)
                .fold(
                    onSuccess = { notificationList ->
                        _notifications.value = notificationList
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun markNotificationAsRead(token: String, notificationId: Long) {
        viewModelScope.launch {
            markNotificationAsReadUseCase(token, notificationId)
                .fold(
                    onSuccess = {
                        // Update local state
                        _notifications.value = _notifications.value.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                        // Update unread count
                        _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
        }
    }
    
    fun markAllNotificationsAsRead(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            markAllNotificationsAsReadUseCase(token)
                .fold(
                    onSuccess = {
                        // Update local state
                        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                        _unreadCount.value = 0
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun deleteNotification(token: String, notificationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            deleteNotificationUseCase(token, notificationId)
                .fold(
                    onSuccess = {
                        _notifications.value = _notifications.value.filter { it.id != notificationId }
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadUnreadCount(token: String) {
        viewModelScope.launch {
            getUnreadNotificationCountUseCase(token)
                .fold(
                    onSuccess = { count ->
                        _unreadCount.value = count
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
        }
    }
    
    fun registerDevice(
        token: String,
        deviceId: String,
        fcmToken: String,
        appVersion: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            registerDeviceUseCase(token, deviceId, fcmToken, appVersion)
                .fold(
                    onSuccess = {
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun observeNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase.getNotificationsFlow()
                .collect { notificationList ->
                    _notifications.value = notificationList
                }
        }
    }
    
    fun observeUnreadCount() {
        viewModelScope.launch {
            getUnreadNotificationCountUseCase.getUnreadCountFlow()
                .collect { count ->
                    _unreadCount.value = count
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
