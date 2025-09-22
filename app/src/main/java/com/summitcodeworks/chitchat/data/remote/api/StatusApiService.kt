package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface StatusApiService {
    
    @POST("api/status/create")
    suspend fun createStatus(
        @Header("Authorization") token: String,
        @Body request: CreateStatusRequest
    ): Response<ApiResponse<StatusDto>>
    
    @GET("api/status/user/{userId}")
    suspend fun getUserStatuses(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<ApiResponse<List<StatusDto>>>
    
    @GET("api/status/active")
    suspend fun getActiveStatuses(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<StatusDto>>>
    
    @GET("api/status/contacts")
    suspend fun getContactsStatuses(
        @Header("Authorization") token: String,
        @Query("contactIds") contactIds: String // comma-separated IDs
    ): Response<ApiResponse<List<StatusDto>>>
    
    @POST("api/status/{statusId}/view")
    suspend fun viewStatus(
        @Header("Authorization") token: String,
        @Path("statusId") statusId: Long
    ): Response<ApiResponse<Unit>>
    
    @POST("api/status/{statusId}/react")
    suspend fun reactToStatus(
        @Header("Authorization") token: String,
        @Path("statusId") statusId: Long,
        @Body request: ReactToStatusRequest
    ): Response<ApiResponse<Unit>>
    
    @DELETE("api/status/{statusId}")
    suspend fun deleteStatus(
        @Header("Authorization") token: String,
        @Path("statusId") statusId: Long
    ): Response<ApiResponse<Unit>>
    
    @GET("api/status/{statusId}/views")
    suspend fun getStatusViews(
        @Header("Authorization") token: String,
        @Path("statusId") statusId: Long
    ): Response<ApiResponse<List<StatusViewDto>>>
}
