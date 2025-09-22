package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import javax.inject.Inject

class ReactToStatusUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(token: String, statusId: Long, reaction: String): Result<Unit> {
        return statusRepository.reactToStatus(token, statusId, reaction)
    }
}
