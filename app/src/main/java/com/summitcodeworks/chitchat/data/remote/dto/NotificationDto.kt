package com.summitcodeworks.chitchat.data.remote.dto

data class NotificationDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val body: String,
    val type: String, // MESSAGE, CALL, STATUS, GROUP_UPDATE
    val data: String? = null, // JSON string for additional data
    val createdAt: String,
    val readAt: String? = null,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false
)

data class SendNotificationRequest(
    val recipientId: Long,
    val title: String,
    val body: String,
    val type: String,
    val data: Map<String, Any>? = null
)

data class SendBulkNotificationRequest(
    val userIds: List<Long>,
    val notification: NotificationContent
)

data class NotificationContent(
    val title: String,
    val body: String,
    val type: String,
    val data: Map<String, Any>? = null
)

data class DeviceTokenRequest(
    val deviceToken: String,
    val deviceType: String, // ANDROID, iOS, WEB
    val deviceId: String
)

data class NotificationPageResponse(
    val content: List<NotificationDto>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)
