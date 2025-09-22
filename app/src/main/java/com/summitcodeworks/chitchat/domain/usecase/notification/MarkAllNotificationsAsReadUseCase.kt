package com.summitcodeworks.chitchat.domain.usecase.notification

import com.summitcodeworks.chitchat.data.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return notificationRepository.markAllAsRead(token)
    }
}
