package com.summitcodeworks.chitchat.domain.usecase.notification

import com.summitcodeworks.chitchat.data.repository.NotificationRepository
import com.summitcodeworks.chitchat.domain.model.Notification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        token: String,
        page: Int = 0,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ): Result<List<Notification>> {
        return notificationRepository.getNotifications(
            token = token,
            page = page,
            limit = limit,
            unreadOnly = unreadOnly
        )
    }
    
    fun getNotificationsFlow(): Flow<List<Notification>> {
        return notificationRepository.getNotificationsFlow()
    }
}
