package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import com.summitcodeworks.chitchat.domain.model.Group
import javax.inject.Inject

class GetGroupDetailsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(token: String, groupId: Long): Result<Group> {
        return groupRepository.getGroupDetails(token, groupId)
    }
}
