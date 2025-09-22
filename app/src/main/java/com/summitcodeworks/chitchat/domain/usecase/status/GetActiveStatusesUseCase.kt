package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import com.summitcodeworks.chitchat.domain.model.Status
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveStatusesUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(token: String): Result<List<Status>> {
        return statusRepository.getActiveStatuses(token)
    }
    
    fun getActiveStatusesFlow(): Flow<List<Status>> {
        return statusRepository.getActiveStatusesFlow()
    }
}
