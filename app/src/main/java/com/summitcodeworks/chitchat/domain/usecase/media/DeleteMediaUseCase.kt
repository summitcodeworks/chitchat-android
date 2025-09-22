package com.summitcodeworks.chitchat.domain.usecase.media

import com.summitcodeworks.chitchat.data.repository.MediaRepository
import javax.inject.Inject

class DeleteMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(token: String, mediaId: Long): Result<Unit> {
        return mediaRepository.deleteMedia(token, mediaId)
    }
}
