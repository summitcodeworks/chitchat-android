package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import javax.inject.Inject

class RemoveGroupMemberUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        token: String,
        groupId: Long,
        memberId: Long
    ): Result<Unit> {
        return groupRepository.removeGroupMember(token, groupId, memberId)
    }
}
