package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.local.dao.NotificationDao
import com.summitcodeworks.chitchat.data.local.entity.NotificationEntity
import com.summitcodeworks.chitchat.data.mapper.NotificationMapper
import com.summitcodeworks.chitchat.data.remote.api.NotificationApiService
import com.summitcodeworks.chitchat.data.remote.api.RegisterDeviceRequest
import com.summitcodeworks.chitchat.data.remote.api.UpdateDeviceTokenRequest
import com.summitcodeworks.chitchat.data.remote.api.NotificationSettingsDto
import com.summitcodeworks.chitchat.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationRepository {
    suspend fun getNotifications(
        token: String,
        page: Int = 0,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ): Result<List<Notification>>
    
    suspend fun getNotification(token: String, notificationId: Long): Result<Notification>
    
    suspend fun markAsRead(token: String, notificationId: Long): Result<Unit>
    
    suspend fun markAllAsRead(token: String): Result<Unit>
    
    suspend fun deleteNotification(token: String, notificationId: Long): Result<Unit>
    
    suspend fun clearAllNotifications(token: String): Result<Unit>
    
    suspend fun getUnreadCount(token: String): Result<Int>
    
    suspend fun registerDevice(
        token: String,
        deviceId: String,
        fcmToken: String,
        appVersion: String
    ): Result<Unit>
    
    suspend fun updateDeviceToken(token: String, deviceId: String, fcmToken: String): Result<Unit>
    
    suspend fun unregisterDevice(token: String, deviceId: String): Result<Unit>
    
    suspend fun getNotificationSettings(token: String): Result<NotificationSettingsDto>
    
    suspend fun updateNotificationSettings(
        token: String,
        settings: NotificationSettingsDto
    ): Result<NotificationSettingsDto>
    
    fun getNotificationsFlow(): Flow<List<Notification>>
    
    fun getUnreadCountFlow(): Flow<Int>
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationApiService: NotificationApiService,
    private val notificationDao: NotificationDao,
    private val notificationMapper: NotificationMapper
) : NotificationRepository {
    
    override suspend fun getNotifications(
        token: String,
        page: Int,
        limit: Int,
        unreadOnly: Boolean
    ): Result<List<Notification>> {
        return try {
            val response = notificationApiService.getNotifications(token, page, limit, unreadOnly)
            if (response.success) {
                val notifications = response.data?.map { notificationMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                notifications.forEach { notification ->
                    notificationDao.insertNotification(notificationMapper.toEntity(notification))
                }
                
                Result.success(notifications)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get notifications"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedNotifications = if (unreadOnly) {
                notificationDao.getUnreadNotifications().map { notificationMapper.toDomain(it) }
            } else {
                notificationDao.getAllNotifications().map { notificationMapper.toDomain(it) }
            }
            
            if (cachedNotifications.isNotEmpty()) {
                Result.success(cachedNotifications)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getNotification(token: String, notificationId: Long): Result<Notification> {
        return try {
            val response = notificationApiService.getNotification(token, notificationId)
            if (response.success) {
                val notification = notificationMapper.toDomain(response.data!!)
                
                // Save to local database
                notificationDao.insertNotification(notificationMapper.toEntity(notification))
                
                Result.success(notification)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get notification"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedNotification = notificationDao.getNotificationById(notificationId)
            if (cachedNotification != null) {
                Result.success(notificationMapper.toDomain(cachedNotification))
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun markAsRead(token: String, notificationId: Long): Result<Unit> {
        return try {
            val response = notificationApiService.markAsRead(token, notificationId)
            if (response.success) {
                // Update local database
                notificationDao.markNotificationAsRead(notificationId)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to mark as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markAllAsRead(token: String): Result<Unit> {
        return try {
            val response = notificationApiService.markAllAsRead(token)
            if (response.success) {
                // Update local database
                notificationDao.markAllAsRead()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to mark all as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNotification(token: String, notificationId: Long): Result<Unit> {
        return try {
            val response = notificationApiService.deleteNotification(token, notificationId)
            if (response.success) {
                // Remove from local database
                notificationDao.deleteNotificationById(notificationId)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to delete notification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllNotifications(token: String): Result<Unit> {
        return try {
            val response = notificationApiService.clearAllNotifications(token)
            if (response.success) {
                // Clear local database
                notificationDao.clearAllNotifications()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to clear notifications"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUnreadCount(token: String): Result<Int> {
        return try {
            val response = notificationApiService.getUnreadCount(token)
            if (response.success) {
                val count = response.data?.unreadCount ?: 0
                
                // Update local count if needed
                val localCount = notificationDao.getUnreadCount()
                if (count != localCount) {
                    // Sync might be needed, but for now just return the local count
                    Result.success(localCount)
                } else {
                    Result.success(count)
                }
            } else {
                Result.failure(Exception(response.message ?: "Failed to get unread count"))
            }
        } catch (e: Exception) {
            // Return local count
            val localCount = notificationDao.getUnreadCount()
            Result.success(localCount)
        }
    }
    
    override suspend fun registerDevice(
        token: String,
        deviceId: String,
        fcmToken: String,
        appVersion: String
    ): Result<Unit> {
        return try {
            val request = RegisterDeviceRequest(
                deviceId = deviceId,
                fcmToken = fcmToken,
                platform = "android",
                appVersion = appVersion
            )
            
            val response = notificationApiService.registerDevice(token, request)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to register device"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateDeviceToken(token: String, deviceId: String, fcmToken: String): Result<Unit> {
        return try {
            val request = UpdateDeviceTokenRequest(fcmToken = fcmToken)
            val response = notificationApiService.updateDeviceToken(token, deviceId, request)
            
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update device token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unregisterDevice(token: String, deviceId: String): Result<Unit> {
        return try {
            val response = notificationApiService.unregisterDevice(token, deviceId)
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to unregister device"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationSettings(token: String): Result<NotificationSettingsDto> {
        return try {
            val response = notificationApiService.getNotificationSettings(token)
            if (response.success) {
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get notification settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNotificationSettings(
        token: String,
        settings: NotificationSettingsDto
    ): Result<NotificationSettingsDto> {
        return try {
            val response = notificationApiService.updateNotificationSettings(token, settings)
            if (response.success) {
                Result.success(response.data!!)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update notification settings"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getNotificationsFlow(): Flow<List<Notification>> {
        return notificationDao.getAllNotificationsFlow().map { entities ->
            entities.map { notificationMapper.toDomain(it) }
        }
    }
    
    override fun getUnreadCountFlow(): Flow<Int> {
        return notificationDao.getUnreadCountFlow()
    }
}
