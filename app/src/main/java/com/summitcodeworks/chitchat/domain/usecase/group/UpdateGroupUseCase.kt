package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import com.summitcodeworks.chitchat.domain.model.Group
import javax.inject.Inject

class UpdateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        token: String,
        groupId: Long,
        name: String? = null,
        description: String? = null,
        isPublic: Boolean? = null
    ): Result<Group> {
        return groupRepository.updateGroup(token, groupId, name, description, isPublic)
    }
}
