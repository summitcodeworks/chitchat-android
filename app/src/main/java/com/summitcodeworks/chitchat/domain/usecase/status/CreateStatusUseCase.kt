package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import com.summitcodeworks.chitchat.domain.model.Status
import javax.inject.Inject

class CreateStatusUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(
        token: String,
        content: String,
        mediaId: Long? = null,
        statusType: String = "TEXT",
        backgroundColor: String? = null,
        font: String? = null,
        privacy: String = "CONTACTS"
    ): Result<Status> {
        return statusRepository.createStatus(
            token = token,
            content = content,
            mediaId = mediaId,
            statusType = statusType,
            backgroundColor = backgroundColor,
            font = font,
            privacy = privacy
        )
    }
}
