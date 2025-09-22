package com.summitcodeworks.chitchat.domain.usecase.media

import com.summitcodeworks.chitchat.data.repository.MediaRepository
import javax.inject.Inject

class DownloadMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(token: String, mediaId: Long): Result<String> {
        return mediaRepository.downloadMedia(token, mediaId)
    }
}
