package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import javax.inject.Inject

class DeleteStatusUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(token: String, statusId: Long): Result<Unit> {
        return statusRepository.deleteStatus(token, statusId)
    }
}
