package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MessageApiService {
    
    @POST("api/messages/send")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>
    
    @GET("api/messages/conversation/{userId}")
    suspend fun getConversationMessages(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "timestamp,desc"
    ): Response<ApiResponse<MessagePageResponse>>
    
    @GET("api/messages/group/{groupId}")
    suspend fun getGroupMessages(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "timestamp,desc"
    ): Response<ApiResponse<MessagePageResponse>>
    
    @GET("api/messages/search")
    suspend fun searchMessages(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): Response<ApiResponse<List<MessageDto>>>
    
    @PUT("api/messages/{messageId}/read")
    suspend fun markMessageAsRead(
        @Header("Authorization") token: String,
        @Path("messageId") messageId: String
    ): Response<ApiResponse<Unit>>
    
    @DELETE("api/messages/{messageId}")
    suspend fun deleteMessage(
        @Header("Authorization") token: String,
        @Path("messageId") messageId: String,
        @Query("deleteForEveryone") deleteForEveryone: Boolean = false
    ): Response<ApiResponse<Unit>>
    
    // Group Management APIs
    @POST("api/messages/groups")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Body request: CreateGroupRequest
    ): Response<ApiResponse<GroupDto>>
    
    @POST("api/messages/groups/{groupId}/members/{userId}")
    suspend fun addMemberToGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>
    
    @DELETE("api/messages/groups/{groupId}/members/{userId}")
    suspend fun removeMemberFromGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>
    
    @PUT("api/messages/groups/{groupId}")
    suspend fun updateGroupInfo(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Query("name") name: String? = null,
        @Query("description") description: String? = null
    ): Response<ApiResponse<GroupDto>>
    
    @GET("api/messages/groups")
    suspend fun getUserGroups(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<GroupDto>>>
    
    @POST("api/messages/groups/{groupId}/leave")
    suspend fun leaveGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<ApiResponse<Unit>>
}
