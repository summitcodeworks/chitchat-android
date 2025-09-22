package com.summitcodeworks.chitchat.domain.usecase.notification

import com.summitcodeworks.chitchat.data.repository.NotificationRepository
import javax.inject.Inject

class RegisterDeviceUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        token: String,
        deviceId: String,
        fcmToken: String,
        appVersion: String
    ): Result<Unit> {
        return notificationRepository.registerDevice(token, deviceId, fcmToken, appVersion)
    }
}
