package com.summitcodeworks.chitchat.domain.usecase.media

import com.summitcodeworks.chitchat.data.repository.MediaRepository
import com.summitcodeworks.chitchat.domain.model.Media
import java.io.File
import javax.inject.Inject

class UploadMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        token: String,
        file: File,
        type: String,
        description: String? = null,
        messageId: String? = null,
        statusId: Long? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<Media> {
        return mediaRepository.uploadMedia(
            token = token,
            file = file,
            type = type,
            description = description,
            messageId = messageId,
            statusId = statusId,
            onProgress = onProgress
        )
    }
}
