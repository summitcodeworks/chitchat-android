package com.summitcodeworks.chitchat.domain.repository

import com.summitcodeworks.chitchat.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface for authentication repositories
 * This allows us to swap between different authentication implementations
 */
interface AuthRepositoryInterface {
    suspend fun sendOtpSms(phoneNumber: String): Result<Unit>
    suspend fun verifyOtpSms(phoneNumber: String, otp: String): Result<OtpAuthResponse>
    suspend fun getUserProfile(): Result<UserDto>
    suspend fun updateUserProfile(name: String, avatarUrl: String? = null, about: String? = null): Result<UserDto>
    suspend fun syncContacts(contacts: List<ContactDto>): Result<List<UserDto>>
    suspend fun blockUser(userId: Long): Result<Unit>
    suspend fun unblockUser(userId: Long): Result<Unit>
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit>
    suspend fun signOut()

    fun getCurrentUser(): UserDto?
    fun getCurrentUserId(): Long?
    fun getCurrentToken(): String?
    fun isUserAuthenticated(): Boolean

    fun observeAuthState(): Flow<Boolean>
}