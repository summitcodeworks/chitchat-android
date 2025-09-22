package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import com.summitcodeworks.chitchat.domain.model.Group
import javax.inject.Inject

class CreateGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        token: String,
        name: String,
        description: String? = null,
        isPublic: Boolean = false,
        memberIds: List<Long> = emptyList()
    ): Result<Group> {
        return groupRepository.createGroup(
            token = token,
            name = name,
            description = description,
            isPublic = isPublic,
            memberIds = memberIds
        )
    }
}
