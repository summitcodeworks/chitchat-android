package com.summitcodeworks.chitchat.domain.usecase.media

import com.summitcodeworks.chitchat.data.repository.MediaRepository
import com.summitcodeworks.chitchat.domain.model.Media
import javax.inject.Inject

class GetMediaInfoUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(token: String, mediaId: Long): Result<Media> {
        return mediaRepository.getMediaInfo(token, mediaId)
    }
}
