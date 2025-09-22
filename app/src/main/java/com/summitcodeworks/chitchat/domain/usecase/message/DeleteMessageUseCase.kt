package com.summitcodeworks.chitchat.domain.usecase.message

import com.summitcodeworks.chitchat.data.repository.MessageRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        token: String,
        messageId: String,
        deleteForEveryone: Boolean = false
    ): Result<Unit> {
        return messageRepository.deleteMessage(token, messageId, deleteForEveryone)
    }
}
