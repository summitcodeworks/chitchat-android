package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.ApiResponse
import com.summitcodeworks.chitchat.data.remote.dto.GroupDto
import com.summitcodeworks.chitchat.data.remote.dto.UserDto
import retrofit2.http.*

interface GroupApiService {
    
    @POST("api/messages/groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: CreateGroupRequest
    ): ApiResponse<GroupDto>
    
    @GET("api/messages/groups")
    suspend fun getUserGroups(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<GroupDto>>
    
    @GET("api/messages/groups/{groupId}")
    suspend fun getGroupDetails(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): ApiResponse<GroupDto>
    
    @PUT("api/messages/groups/{groupId}")
    suspend fun updateGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Body request: UpdateGroupRequest
    ): ApiResponse<GroupDto>
    
    @DELETE("api/messages/groups/{groupId}")
    suspend fun deleteGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): ApiResponse<Unit>
    
    @POST("api/messages/groups/{groupId}/members")
    suspend fun addGroupMembers(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Body request: AddMembersRequest
    ): ApiResponse<List<GroupMemberDto>>
    
    @GET("api/messages/groups/{groupId}/members")
    suspend fun getGroupMembers(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 50
    ): ApiResponse<List<GroupMemberDto>>
    
    @PUT("api/messages/groups/{groupId}/members/{memberId}")
    suspend fun updateMemberRole(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long,
        @Body request: UpdateMemberRoleRequest
    ): ApiResponse<GroupMemberDto>
    
    @DELETE("api/messages/groups/{groupId}/members/{memberId}")
    suspend fun removeGroupMember(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Path("memberId") memberId: Long
    ): ApiResponse<Unit>
    
    @POST("api/messages/groups/{groupId}/join")
    suspend fun joinGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): ApiResponse<GroupMemberDto>
    
    @POST("api/messages/groups/{groupId}/leave")
    suspend fun leaveGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): ApiResponse<Unit>
    
    @POST("api/messages/groups/{groupId}/invite")
    suspend fun inviteToGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Body request: InviteToGroupRequest
    ): ApiResponse<InvitationResponse>
    
    @GET("api/messages/groups/search")
    suspend fun searchGroups(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<GroupDto>>
}

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val memberIds: List<Long> = emptyList()
)

data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null,
    val isPublic: Boolean? = null
)

data class AddMembersRequest(
    val memberIds: List<Long>
)

data class UpdateMemberRoleRequest(
    val role: String // "admin", "member"
)

data class InviteToGroupRequest(
    val phoneNumbers: List<String>
)

data class InvitationResponse(
    val sent: Int,
    val failed: Int,
    val details: List<InvitationDetail>
)

data class InvitationDetail(
    val phoneNumber: String,
    val success: Boolean,
    val message: String
)

data class GroupMemberDto(
    val id: Long,
    val groupId: Long,
    val userId: Long,
    val role: String,
    val joinedAt: String,
    val user: UserDto
)
