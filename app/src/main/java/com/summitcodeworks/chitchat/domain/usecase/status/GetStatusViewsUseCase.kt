package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import com.summitcodeworks.chitchat.domain.model.StatusView
import javax.inject.Inject

class GetStatusViewsUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(token: String, statusId: Long): Result<List<StatusView>> {
        return statusRepository.getStatusViews(token, statusId)
    }
}
