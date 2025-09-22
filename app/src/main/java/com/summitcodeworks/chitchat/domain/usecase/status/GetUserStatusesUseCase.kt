package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import com.summitcodeworks.chitchat.domain.model.Status
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserStatusesUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(token: String, userId: Long): Result<List<Status>> {
        return statusRepository.getUserStatuses(token, userId)
    }
    
    fun getUserStatusesFlow(userId: Long): Flow<List<Status>> {
        return statusRepository.getUserStatusesFlow(userId)
    }
}
