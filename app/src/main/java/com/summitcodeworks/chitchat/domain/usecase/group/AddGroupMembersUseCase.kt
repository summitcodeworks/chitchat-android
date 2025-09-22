package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import com.summitcodeworks.chitchat.domain.model.Group
import javax.inject.Inject

class AddGroupMembersUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        token: String,
        groupId: Long,
        memberIds: List<Long>
    ): Result<Unit> {
        return groupRepository.addGroupMembers(token, groupId, memberIds)
    }
}
