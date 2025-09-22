package com.summitcodeworks.chitchat.domain.usecase.notification

import com.summitcodeworks.chitchat.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(token: String): Result<Int> {
        return notificationRepository.getUnreadCount(token)
    }
    
    fun getUnreadCountFlow(): Flow<Int> {
        return notificationRepository.getUnreadCountFlow()
    }
}
