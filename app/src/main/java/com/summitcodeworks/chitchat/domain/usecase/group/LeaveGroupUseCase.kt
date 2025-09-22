package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import javax.inject.Inject

class LeaveGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(token: String, groupId: Long): Result<Unit> {
        return groupRepository.leaveGroup(token, groupId)
    }
}
