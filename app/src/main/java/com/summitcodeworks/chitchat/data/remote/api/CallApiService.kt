package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CallApiService {
    
    @POST("api/calls/initiate")
    suspend fun initiateCall(
        @Header("Authorization") token: String,
        @Body request: InitiateCallRequest
    ): Response<ApiResponse<CallDto>>
    
    @POST("api/calls/{sessionId}/answer")
    suspend fun answerCall(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String,
        @Body request: AnswerCallRequest
    ): Response<ApiResponse<CallDto>>
    
    @POST("api/calls/{sessionId}/reject")
    suspend fun rejectCall(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String,
        @Query("reason") reason: String? = null
    ): Response<ApiResponse<CallDto>>
    
    @POST("api/calls/{sessionId}/end")
    suspend fun endCall(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String,
        @Query("reason") reason: String? = null
    ): Response<ApiResponse<CallDto>>
    
    @GET("api/calls/{sessionId}")
    suspend fun getCallDetails(
        @Header("Authorization") token: String,
        @Path("sessionId") sessionId: String
    ): Response<ApiResponse<CallDto>>
    
    @GET("api/calls/history")
    suspend fun getCallHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "startTime,desc"
    ): Response<ApiResponse<CallPageResponse>>
    
    @GET("api/calls/missed")
    suspend fun getMissedCalls(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<CallDto>>>
    
    @GET("api/calls/recent")
    suspend fun getRecentCalls(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<CallDto>>>
}
