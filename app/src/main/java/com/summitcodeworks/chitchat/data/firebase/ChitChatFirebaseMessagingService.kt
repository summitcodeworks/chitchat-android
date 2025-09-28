package com.summitcodeworks.chitchat.data.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.domain.usecase.notification.UpdateDeviceTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Firebase Messaging Service for handling push notifications and token refresh
 */
@AndroidEntryPoint
class ChitChatFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var firebaseTokenManager: FirebaseTokenManager

    @Inject
    lateinit var otpAuthManager: OtpAuthManager

    @Inject
    lateinit var updateDeviceTokenUseCase: UpdateDeviceTokenUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "ChitChatFCMService"
    }

    /**
     * Called when a new Firebase token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Firebase token received: ${token.take(20)}...")

        // Save the new token locally
        firebaseTokenManager.clearSavedToken()
        serviceScope.launch {
            firebaseTokenManager.getDeviceToken() // This will save the new token
        }

        // Update the token on the server if user is authenticated
        val currentAuthToken = otpAuthManager.currentToken.value
        val isAuthenticated = otpAuthManager.isAuthenticated.value

        if (isAuthenticated && !currentAuthToken.isNullOrBlank()) {
            serviceScope.launch {
                try {
                    Log.d(TAG, "Updating new device token on server...")

                    val result = updateDeviceTokenUseCase(currentAuthToken)

                    result.fold(
                        onSuccess = { user ->
                            Log.d(TAG, "New device token updated successfully on server for user: ${user.name}")
                        },
                        onFailure = { exception ->
                            Log.w(TAG, "Failed to update new device token on server: ${exception.message}", exception)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Exception while updating new device token on server", e)
                }
            }
        } else {
            Log.d(TAG, "User not authenticated, new token will be updated on next login")
        }
    }

    /**
     * Called when a push notification is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Handle notification data
        remoteMessage.data.isNotEmpty().let { hasData ->
            if (hasData) {
                Log.d(TAG, "Message data payload: ${remoteMessage.data}")
                handleDataMessage(remoteMessage.data)
            }
        }

        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message notification: ${notification.title} - ${notification.body}")
            handleNotificationMessage(notification)
        }
    }

    /**
     * Handles data-only messages (when app is in foreground or background)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "MESSAGE" -> {
                // Handle new message notification
                Log.d(TAG, "New message notification received")
                // TODO: Update message UI, show in-app notification
            }
            "CALL" -> {
                // Handle incoming call notification
                Log.d(TAG, "Incoming call notification received")
                // TODO: Show call screen or call notification
            }
            "GROUP_MESSAGE" -> {
                // Handle group message notification
                Log.d(TAG, "Group message notification received")
                // TODO: Update group message UI
            }
            else -> {
                Log.d(TAG, "Unknown notification type: ${data["type"]}")
            }
        }
    }

    /**
     * Handles display notifications (when app is in background)
     */
    private fun handleNotificationMessage(notification: com.google.firebase.messaging.RemoteMessage.Notification) {
        // The system automatically handles display notifications when app is in background
        // This method is for additional processing if needed
        Log.d(TAG, "Display notification: ${notification.title}")
    }
}