package com.summitcodeworks.chitchat.domain.usecase.media

import com.summitcodeworks.chitchat.data.repository.MediaRepository
import com.summitcodeworks.chitchat.domain.model.Media
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository
) {
    suspend operator fun invoke(
        token: String,
        userId: Long,
        type: String? = null,
        page: Int = 0,
        limit: Int = 20
    ): Result<List<Media>> {
        return mediaRepository.getUserMedia(token, userId, type, page, limit)
    }
    
    fun getUserMediaFlow(userId: Long): Flow<List<Media>> {
        return mediaRepository.getUserMediaFlow(userId)
    }
}
