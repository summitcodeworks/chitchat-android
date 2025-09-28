package com.summitcodeworks.chitchat.data.remote.dto

data class RegisterDeviceRequest(
    val deviceToken: String,
    val deviceType: String, // "ANDROID", "iOS", "WEB"
    val deviceId: String
)

data class UpdateDeviceTokenRequest(
    val oldToken: String,
    val newToken: String,
    val deviceType: String
)

data class DeviceTokenUpdateRequest(
    val deviceToken: String
)

data class NotificationSettingsDto(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val messageNotifications: Boolean = true,
    val callNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val statusNotifications: Boolean = true
)
