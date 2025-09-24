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
    suspend fun getUserProfile(): Response<ApiResponse<UserDto>>

    @PUT("api/users/profile")
    suspend fun updateUserProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<UserDto>>

    @POST("api/users/contacts/sync")
    suspend fun syncContacts(
        @Body request: ContactSyncRequest
    ): Response<ApiResponse<List<UserDto>>>

    @POST("api/users/block/{userId}")
    suspend fun blockUser(
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>

    @DELETE("api/users/block/{userId}")
    suspend fun unblockUser(
        @Path("userId") userId: Long
    ): Response<ApiResponse<Unit>>

    @GET("api/users/blocked")
    suspend fun getBlockedUsers(): Response<ApiResponse<List<UserDto>>>

    @PUT("api/users/status")
    suspend fun updateOnlineStatus(
        @Query("isOnline") isOnline: Boolean
    ): Response<ApiResponse<Unit>>

    @GET("api/users/phone/{phoneNumber}")
    suspend fun getUserByPhoneNumber(
        @Path("phoneNumber") phoneNumber: String
    ): Response<ApiResponse<UserDto>>

    @GET("api/users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: Long
    ): Response<ApiResponse<UserDto>>

    @GET("api/users")
    suspend fun getAllUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<ApiResponse<UserPageResponse>>
}
