package com.summitcodeworks.chitchat.data.repository

// Firebase imports removed - using OTP authentication instead
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.data.local.dao.UserDao
import com.summitcodeworks.chitchat.data.local.entity.UserEntity
import com.summitcodeworks.chitchat.data.mapper.UserMapper
import com.summitcodeworks.chitchat.data.remote.api.UserApiService
import com.summitcodeworks.chitchat.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling authentication-related data operations in ChitChat.
 * 
 * This repository serves as the single source of truth for authentication data,
 * managing both local database operations and remote API calls. It integrates
 * with the OTP authentication system and handles user profile management.
 * 
 * Key responsibilities:
 * - User profile retrieval and caching
 * - Local user data management
 * - Integration with OTP authentication manager
 * - User data synchronization between local and remote sources
 * - Error handling for authentication-related operations
 * 
 * The repository follows the Repository pattern to:
 * - Abstract data source complexity from ViewModels
 * - Provide a unified interface for authentication data
 * - Handle caching and offline data access
 * - Manage data transformation between DTOs and entities
 * 
 * Note: Firebase authentication methods have been removed in favor of
 * OTP-based authentication system for improved security and user experience.
 * 
 * @param userApiService API service for remote user operations
 * @param userDao Data access object for local user database operations
 * @param userMapper Mapper for converting between DTOs and entities
 * @param otpAuthManager Authentication manager for OTP-based auth
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@Singleton
class AuthRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val userDao: UserDao,
    private val userMapper: UserMapper,
    private val otpAuthManager: OtpAuthManager
) {

    /**
     * Retrieves the current user's profile from the server and caches it locally.
     * 
     * This method fetches the complete user profile from the API and stores it
     * in the local database for offline access. The authentication token is
     * automatically added by the OtpAuthInterceptor.
     * 
     * @return Result containing UserDto on success or Exception on failure
     */
    suspend fun getUserProfile(): Result<UserDto> {
        return try {
            // Token is automatically added by OtpAuthInterceptor
            val response = userApiService.getUserProfile()

            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    // Update local database
                    val userEntity = userMapper.dtoToEntity(userDto)
                    userDao.insertUser(userEntity)

                    Result.success(userDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get user profile"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(name: String, avatarUrl: String? = null, about: String? = null): Result<UserDto> {
        return try {
            val request = UpdateProfileRequest(
                name = name,
                avatarUrl = avatarUrl,
                about = about
            )

            // Token is automatically added by OtpAuthInterceptor
            val response = userApiService.updateUserProfile(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    // Update local database
                    val userEntity = userMapper.dtoToEntity(userDto)
                    userDao.insertUser(userEntity)
                    
                    Result.success(userDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to update profile"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncContacts(contacts: List<ContactDto>): Result<List<UserDto>> {
        return try {
            // Clean phone numbers in contacts before sending to API
            val cleanedContacts = contacts.map { contact ->
                contact.copy(phoneNumber = cleanPhoneNumber(contact.phoneNumber))
            }
            
            val request = ContactSyncRequest(contacts = cleanedContacts)
            // Token is automatically added by OtpAuthInterceptor
            val response = userApiService.syncContacts(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val users = response.body()?.data
                if (users != null) {
                    // Save contacts to local database
                    val userEntities = users.map { userMapper.dtoToEntity(it).copy(isContact = true) }
                    userDao.insertUsers(userEntities)
                    
                    Result.success(users)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to sync contacts"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun blockUser(userId: Long): Result<Unit> {
        return try {
            // Token is automatically added by OtpAuthInterceptor
            val response = userApiService.blockUser(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database
                userDao.updateUserBlockStatus(userId, true)
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to block user"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unblockUser(userId: Long): Result<Unit> {
        return try {
            // Token is automatically added by OtpAuthInterceptor
            val response = userApiService.unblockUser(userId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database
                userDao.updateUserBlockStatus(userId, false)
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to unblock user"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            // Token is automatically added by OtpAuthInterceptor
            val response = userApiService.updateOnlineStatus(isOnline)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database if needed
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to update status"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): UserDto? {
        return otpAuthManager.getCurrentUser()
    }

    fun getCurrentUserId(): Long? {
        return otpAuthManager.getCurrentUserId()
    }

    suspend fun getCurrentUserToken(): String? {
        return otpAuthManager.getCurrentToken()
    }

    suspend fun signOut() {
        otpAuthManager.signOut()
        userDao.deleteAllUsers()
    }

    fun observeCurrentUser(): Flow<UserEntity?> {
        val currentUserId = getCurrentUserId()
        return if (currentUserId != null) {
            flow {
                val user = userDao.getUserById(currentUserId)
                emit(user)
            }
        } else {
            flow { emit(null) }
        }
    }

    fun observeAuthState(): Flow<Boolean> = otpAuthManager.isAuthenticated
    
    // OTP-based Authentication Methods (Primary)
    suspend fun sendOtpSms(phoneNumber: String): Result<Unit> {
        return try {
            val cleanedPhone = cleanPhoneNumber(phoneNumber)
            val request = SendOtpRequest(phoneNumber = cleanedPhone)
            val response = userApiService.sendOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to send OTP"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyOtpSms(phoneNumber: String, otp: String): Result<OtpAuthResponse> {
        return try {
            val cleanedPhone = cleanPhoneNumber(phoneNumber)
            val request = VerifyOtpSmsRequest(phoneNumber = cleanedPhone, otp = otp)
            val response = userApiService.verifyOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()?.data
                if (authResponse != null) {
                    // Set authentication data in OtpAuthManager
                    otpAuthManager.setAuthData(authResponse)
                    
                    // Save user to local database
                    val userEntity = userMapper.dtoToEntity(authResponse.user)
                    userDao.insertUser(userEntity)
                    
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "OTP verification failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cleans and formats a phone number to international format
     * - Removes all formatting characters (spaces, dashes, parentheses, dots, etc.)
     * - Ensures it starts with + sign
     * - Returns clean format: +[country code][number]
     */
    private fun cleanPhoneNumber(phone: String): String {
        // Remove all formatting characters except + and digits
        var cleaned = phone.replace(Regex("[^+\\d]"), "")
        
        // Ensure only one + at the beginning
        if (cleaned.startsWith("+")) {
            // Remove any additional + signs after the first one
            cleaned = "+" + cleaned.substring(1).replace("+", "")
        } else if (cleaned.isNotEmpty() && cleaned[0].isDigit()) {
            // If no + sign but starts with digit, add + at the beginning
            cleaned = "+$cleaned"
        }
        
        return cleaned
    }

    suspend fun updateDeviceToken(token: String, request: DeviceTokenUpdateRequest): Result<UserDto> {
        return try {
            // Token is automatically added by OtpAuthInterceptor, but we can override with specific token if needed
            val response = userApiService.updateDeviceToken(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    // Update local database
                    val userEntity = userMapper.dtoToEntity(userDto)
                    userDao.insertUser(userEntity)

                    Result.success(userDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to update device token"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
