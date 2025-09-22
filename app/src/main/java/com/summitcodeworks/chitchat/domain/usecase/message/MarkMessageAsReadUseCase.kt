package com.summitcodeworks.chitchat.domain.usecase.message

import com.summitcodeworks.chitchat.data.repository.MessageRepository
import javax.inject.Inject

class MarkMessageAsReadUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(token: String, messageId: String): Result<Unit> {
        return messageRepository.markMessageAsRead(token, messageId)
    }
}
