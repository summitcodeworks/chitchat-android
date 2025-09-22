package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {
    
    @POST("api/users/authenticate")
    suspend fun authenticateWithFirebase(
        @Body request: FirebaseAuthRequest
    ): Response<ApiResponse<FirebaseAuthResponse>>
    
    @GET("api/users/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserDto>>
    
    @PUT("api/users/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>
    
    @POST("api/users/contacts/sync")
    suspend fun syncContacts(
        @Header("Authorization") token: String,
        @Body request: ContactSyncRequest
    ): Response<ApiResponse<List<UserDto>>>
    
    @POST("api/users/block/{userId}")
    suspend fun blockUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>
    
    @DELETE("api/users/block/{userId}")
    suspend fun unblockUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>
    
    @GET("api/users/blocked")
    suspend fun getBlockedUsers(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<UserDto>>>
    
    @PUT("api/users/status")
    suspend fun updateOnlineStatus(
        @Header("Authorization") token: String,
        @Query("isOnline") isOnline: Boolean
    ): Response<ApiResponse<Unit>>
    
    @GET("api/users/phone/{phoneNumber}")
    suspend fun getUserByPhoneNumber(
        @Header("Authorization") token: String,
        @Path("phoneNumber") phoneNumber: String
    ): Response<ApiResponse<UserDto>>
    
    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<ApiResponse<UserDto>>
    
    @GET("api/users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<ApiResponse<UserPageResponse>>
}
