package com.summitcodeworks.chitchat.domain.usecase.group

import com.summitcodeworks.chitchat.data.repository.GroupRepository
import com.summitcodeworks.chitchat.domain.model.Group
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(
        token: String,
        page: Int = 0,
        limit: Int = 20
    ): Result<List<Group>> {
        return groupRepository.getUserGroups(token, page, limit)
    }
    
    fun getUserGroupsFlow(): Flow<List<Group>> {
        return groupRepository.getUserGroupsFlow()
    }
}
