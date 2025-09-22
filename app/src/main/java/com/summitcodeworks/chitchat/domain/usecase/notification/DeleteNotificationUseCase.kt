package com.summitcodeworks.chitchat.domain.usecase.notification

import com.summitcodeworks.chitchat.data.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(token: String, notificationId: Long): Result<Unit> {
        return notificationRepository.deleteNotification(token, notificationId)
    }
}
