package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.local.dao.GroupDao
import com.summitcodeworks.chitchat.data.local.dao.GroupMemberDao
import com.summitcodeworks.chitchat.data.local.entity.GroupEntity
import com.summitcodeworks.chitchat.data.local.entity.GroupMemberEntity
import com.summitcodeworks.chitchat.data.mapper.GroupMapper
import com.summitcodeworks.chitchat.data.remote.api.GroupApiService
import com.summitcodeworks.chitchat.data.remote.api.CreateGroupRequest
import com.summitcodeworks.chitchat.data.remote.api.UpdateGroupRequest
import com.summitcodeworks.chitchat.data.remote.api.AddMembersRequest
import com.summitcodeworks.chitchat.data.remote.api.UpdateMemberRoleRequest
import com.summitcodeworks.chitchat.data.remote.api.InviteToGroupRequest
import com.summitcodeworks.chitchat.data.remote.dto.*
import com.summitcodeworks.chitchat.domain.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

interface GroupRepository {
    suspend fun createGroup(
        token: String,
        name: String,
        description: String?,
        isPublic: Boolean,
        memberIds: List<Long>
    ): Result<Group>
    
    suspend fun getUserGroups(token: String, page: Int = 0, limit: Int = 20): Result<List<Group>>
    
    suspend fun getGroupDetails(token: String, groupId: Long): Result<Group>
    
    suspend fun updateGroup(
        token: String,
        groupId: Long,
        name: String?,
        description: String?,
        isPublic: Boolean?
    ): Result<Group>
    
    suspend fun deleteGroup(token: String, groupId: Long): Result<Unit>
    
    suspend fun addGroupMembers(token: String, groupId: Long, memberIds: List<Long>): Result<Unit>
    
    suspend fun getGroupMembers(token: String, groupId: Long, page: Int = 0, limit: Int = 50): Result<List<com.summitcodeworks.chitchat.data.remote.api.GroupMemberDto>>
    
    suspend fun updateMemberRole(
        token: String,
        groupId: Long,
        memberId: Long,
        role: String
    ): Result<Group>
    
    suspend fun removeGroupMember(token: String, groupId: Long, memberId: Long): Result<Unit>
    
    suspend fun joinGroup(token: String, groupId: Long): Result<Group>
    
    suspend fun leaveGroup(token: String, groupId: Long): Result<Unit>
    
    suspend fun inviteToGroup(
        token: String,
        groupId: Long,
        phoneNumbers: List<String>
    ): Result<Map<String, Any>>
    
    suspend fun searchGroups(token: String, query: String, page: Int = 0, limit: Int = 20): Result<List<Group>>
    
    fun getUserGroupsFlow(): Flow<List<Group>>
    
    fun getGroupMembersFlow(groupId: Long): Flow<List<Group>>
}

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupApiService: GroupApiService,
    private val groupDao: GroupDao,
    private val groupMemberDao: GroupMemberDao,
    private val groupMapper: GroupMapper
) : GroupRepository {
    
    override suspend fun createGroup(
        token: String,
        name: String,
        description: String?,
        isPublic: Boolean,
        memberIds: List<Long>
    ): Result<Group> {
        return try {
            val request = CreateGroupRequest(
                name = name,
                description = description,
                isPublic = isPublic,
                memberIds = memberIds
            )
            
            val response = groupApiService.createGroup(token, request)
            if (response.success) {
                val group = groupMapper.toDomain(response.data!!)
                
                // Save to local database
                groupDao.insertGroup(groupMapper.toEntity(group))
                
                Result.success(group)
            } else {
                Result.failure(Exception(response.message ?: "Failed to create group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserGroups(token: String, page: Int, limit: Int): Result<List<Group>> {
        return try {
            val response = groupApiService.getUserGroups(token, page, limit)
            if (response.success) {
                val groups = response.data?.map { groupMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                groups.forEach { group ->
                    groupDao.insertGroup(groupMapper.toEntity(group))
                }
                
                Result.success(groups)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get groups"))
            }
        } catch (e: Exception) {
            // Return cached data if available - we'll need to implement a way to get all groups
            Result.failure(e)
        }
    }
    
    override suspend fun getGroupDetails(token: String, groupId: Long): Result<Group> {
        return try {
            val response = groupApiService.getGroupDetails(token, groupId)
            if (response.success) {
                val group = groupMapper.toDomain(response.data!!)
                
                // Save to local database
                groupDao.insertGroup(groupMapper.toEntity(group))
                
                Result.success(group)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get group details"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedGroup = groupDao.getGroupById(groupId)
            if (cachedGroup != null) {
                Result.success(groupMapper.toDomain(cachedGroup))
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun updateGroup(
        token: String,
        groupId: Long,
        name: String?,
        description: String?,
        isPublic: Boolean?
    ): Result<Group> {
        return try {
            val request = UpdateGroupRequest(
                name = name,
                description = description,
                isPublic = isPublic
            )
            
            val response = groupApiService.updateGroup(token, groupId, request)
            if (response.success) {
                val group = groupMapper.toDomain(response.data!!)
                
                // Update local database
                groupDao.insertGroup(groupMapper.toEntity(group))
                
                Result.success(group)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteGroup(token: String, groupId: Long): Result<Unit> {
        return try {
            val response = groupApiService.deleteGroup(token, groupId)
            if (response.success) {
                // Remove from local database
                groupDao.deleteGroupById(groupId)
                groupMemberDao.deleteGroupMembers(groupId)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to delete group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addGroupMembers(token: String, groupId: Long, memberIds: List<Long>): Result<Unit> {
        return try {
            val request = AddMembersRequest(memberIds = memberIds)
            val response = groupApiService.addGroupMembers(token, groupId, request)
            
            if (response.success) {
                // Update local database with new members
                response.data?.forEach { memberDto ->
                    groupMemberDao.insertGroupMember(
                        GroupMemberEntity(
                            groupId = memberDto.groupId,
                            userId = memberDto.userId,
                            role = memberDto.role,
                            joinedAt = memberDto.joinedAt
                        )
                    )
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to add members"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGroupMembers(token: String, groupId: Long, page: Int, limit: Int): Result<List<com.summitcodeworks.chitchat.data.remote.api.GroupMemberDto>> {
        return try {
            val response = groupApiService.getGroupMembers(token, groupId, page, limit)
            if (response.success) {
                // Save members to local database
                response.data?.forEach { memberDto ->
                    groupMemberDao.insertGroupMember(
                        GroupMemberEntity(
                            groupId = memberDto.groupId,
                            userId = memberDto.userId,
                            role = memberDto.role,
                            joinedAt = memberDto.joinedAt
                        )
                    )
                }
                
                Result.success(response.data ?: emptyList())
            } else {
                Result.failure(Exception(response.message ?: "Failed to get group members"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedMembers = groupMemberDao.getGroupMembers(groupId)
            if (cachedMembers.isNotEmpty()) {
                Result.success(emptyList()) // Simplified for now
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun updateMemberRole(
        token: String,
        groupId: Long,
        memberId: Long,
        role: String
    ): Result<Group> {
        return try {
            val request = UpdateMemberRoleRequest(role = role)
            val response = groupApiService.updateMemberRole(token, groupId, memberId, request)
            
            if (response.success) {
                // Update local database
                val member = groupMemberDao.getGroupMember(groupId, memberId)
                member?.let {
                    groupMemberDao.insertGroupMember(
                        it.copy(role = role)
                    )
                }
                
                // Return updated group
                getGroupDetails(token, groupId)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update member role"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun removeGroupMember(token: String, groupId: Long, memberId: Long): Result<Unit> {
        return try {
            val response = groupApiService.removeGroupMember(token, groupId, memberId)
            if (response.success) {
                // Remove from local database
                groupMemberDao.removeGroupMember(groupId, memberId)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to remove member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun joinGroup(token: String, groupId: Long): Result<Group> {
        return try {
            val response = groupApiService.joinGroup(token, groupId)
            if (response.success) {
                // Save member info to local database
                response.data?.let { memberDto ->
                    groupMemberDao.insertGroupMember(
                        GroupMemberEntity(
                            groupId = memberDto.groupId,
                            userId = memberDto.userId,
                            role = memberDto.role,
                            joinedAt = memberDto.joinedAt
                        )
                    )
                }
                
                // Return group details
                getGroupDetails(token, groupId)
            } else {
                Result.failure(Exception(response.message ?: "Failed to join group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun leaveGroup(token: String, groupId: Long): Result<Unit> {
        return try {
            val response = groupApiService.leaveGroup(token, groupId)
            if (response.success) {
                // Remove current user from local database
                // Note: We need the current user ID here, which should be passed or obtained from auth
                // For now, this is a simplified implementation

                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to leave group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun inviteToGroup(
        token: String,
        groupId: Long,
        phoneNumbers: List<String>
    ): Result<Map<String, Any>> {
        return try {
            val request = InviteToGroupRequest(phoneNumbers = phoneNumbers)
            val response = groupApiService.inviteToGroup(token, groupId, request)
            
            if (response.success) {
                val invitationResponse = response.data
                val result = mapOf(
                    "sent" to (invitationResponse?.sent ?: 0),
                    "failed" to (invitationResponse?.failed ?: 0),
                    "details" to (invitationResponse?.details ?: emptyList<Any>())
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message ?: "Failed to invite to group"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchGroups(token: String, query: String, page: Int, limit: Int): Result<List<Group>> {
        return try {
            val response = groupApiService.searchGroups(token, query, page, limit)
            if (response.success) {
                val groups = response.data?.map { groupMapper.toDomain(it) } ?: emptyList()
                Result.success(groups)
            } else {
                Result.failure(Exception(response.message ?: "Failed to search groups"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserGroupsFlow(): Flow<List<Group>> {
        // We need a userId to get user groups, this is a simplified version
        // In a real implementation, you'd need to pass userId or get it from somewhere
        return flowOf(emptyList())
    }
    
    override fun getGroupMembersFlow(groupId: Long): Flow<List<Group>> {
        // This should return Group entities with member info, simplified for now
        return flowOf(emptyList())
    }
}
