package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.data.remote.websocket.MultiWebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing WebSocket connections in ChitChat.
 * 
 * This ViewModel provides a simplified interface for managing WebSocket connections
 * throughout the application. It acts as a bridge between the UI layer and the
 * MultiWebSocketManager, providing connection status information and lifecycle management.
 * 
 * Key responsibilities:
 * - Establish WebSocket connections for real-time communication
 * - Monitor connection status and provide it to UI components
 * - Handle connection lifecycle (connect/disconnect)
 * - Ensure proper cleanup when ViewModel is destroyed
 * 
 * The WebSocket connection is essential for:
 * - Real-time messaging between users
 * - Typing indicators
 * - Online status updates
 * - Push notification delivery
 * 
 * @param webSocketManager The core WebSocket manager that handles actual connections
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@HiltViewModel
class WebSocketViewModel @Inject constructor(
    private val webSocketManager: MultiWebSocketManager
) : ViewModel() {
    
    // Current WebSocket connection status
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    /**
     * Establishes WebSocket connections for real-time communication.
     * 
     * This method initiates connections to all required WebSocket endpoints
     * for the ChitChat application. The token is used for authentication
     * with the WebSocket servers.
     * 
     * @param token Authentication token for WebSocket authentication
     */
    fun connectWebSocket(token: String) {
        viewModelScope.launch {
            android.util.Log.d("WebSocketViewModel", "Connecting WebSocket with token...")
            webSocketManager.connectAll()
            _isConnected.value = webSocketManager.isConnected
            android.util.Log.d("WebSocketViewModel", "WebSocket connected: ${webSocketManager.isConnected}")
        }
    }
    
    /**
     * Disconnects all WebSocket connections.
     * 
     * This method closes all active WebSocket connections and updates
     * the connection status. It should be called when the user logs out
     * or when the app goes to background.
     */
    fun disconnectWebSocket() {
        webSocketManager.disconnectAll()
        _isConnected.value = false
    }
    
    /**
     * Checks and updates the current WebSocket connection status.
     * 
     * This method queries the WebSocket manager for the current connection
     * status and updates the UI state accordingly. It can be called to
     * refresh the connection status display.
     */
    fun checkConnection() {
        _isConnected.value = webSocketManager.isConnected
    }
    
    /**
     * Called when the ViewModel is being cleared.
     * 
     * This method ensures that all WebSocket connections are properly
     * closed when the ViewModel is destroyed to prevent memory leaks
     * and unnecessary network connections.
     */
    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}
