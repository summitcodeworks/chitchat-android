package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.ApiResponse
import com.summitcodeworks.chitchat.data.remote.dto.NotificationDto
import retrofit2.http.*

interface NotificationApiService {
    
    @GET("notifications")
    suspend fun getNotifications(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("unread_only") unreadOnly: Boolean = false
    ): ApiResponse<List<NotificationDto>>
    
    @GET("notifications/{notificationId}")
    suspend fun getNotification(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: Long
    ): ApiResponse<NotificationDto>
    
    @PUT("notifications/{notificationId}/read")
    suspend fun markAsRead(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: Long
    ): ApiResponse<Unit>
    
    @PUT("notifications/read-all")
    suspend fun markAllAsRead(
        @Header("Authorization") token: String
    ): ApiResponse<Unit>
    
    @DELETE("notifications/{notificationId}")
    suspend fun deleteNotification(
        @Header("Authorization") token: String,
        @Path("notificationId") notificationId: Long
    ): ApiResponse<Unit>
    
    @DELETE("notifications/clear-all")
    suspend fun clearAllNotifications(
        @Header("Authorization") token: String
    ): ApiResponse<Unit>
    
    @GET("notifications/count")
    suspend fun getUnreadCount(
        @Header("Authorization") token: String
    ): ApiResponse<NotificationCountResponse>
    
    @POST("notifications/register-device")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body request: RegisterDeviceRequest
    ): ApiResponse<Unit>
    
    @PUT("notifications/device/{deviceId}")
    suspend fun updateDeviceToken(
        @Header("Authorization") token: String,
        @Path("deviceId") deviceId: String,
        @Body request: UpdateDeviceTokenRequest
    ): ApiResponse<Unit>
    
    @DELETE("notifications/device/{deviceId}")
    suspend fun unregisterDevice(
        @Header("Authorization") token: String,
        @Path("deviceId") deviceId: String
    ): ApiResponse<Unit>
    
    @GET("notifications/settings")
    suspend fun getNotificationSettings(
        @Header("Authorization") token: String
    ): ApiResponse<NotificationSettingsDto>
    
    @PUT("notifications/settings")
    suspend fun updateNotificationSettings(
        @Header("Authorization") token: String,
        @Body settings: NotificationSettingsDto
    ): ApiResponse<NotificationSettingsDto>
}

data class NotificationCountResponse(
    val unreadCount: Int
)

data class RegisterDeviceRequest(
    val deviceId: String,
    val fcmToken: String,
    val platform: String = "android",
    val appVersion: String
)

data class UpdateDeviceTokenRequest(
    val fcmToken: String
)

data class NotificationSettingsDto(
    val pushNotifications: Boolean = true,
    val messageNotifications: Boolean = true,
    val callNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val statusNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val silentMode: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null
)