package com.summitcodeworks.chitchat.domain.usecase.status

import com.summitcodeworks.chitchat.data.repository.StatusRepository
import com.summitcodeworks.chitchat.domain.model.Status
import javax.inject.Inject

class GetContactsStatusesUseCase @Inject constructor(
    private val statusRepository: StatusRepository
) {
    suspend operator fun invoke(token: String, contactIds: List<Long>): Result<List<Status>> {
        return statusRepository.getContactsStatuses(token, contactIds)
    }
}
