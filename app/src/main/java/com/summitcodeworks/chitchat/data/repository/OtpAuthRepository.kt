package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.data.local.dao.UserDao
import com.summitcodeworks.chitchat.data.local.entity.UserEntity
import com.summitcodeworks.chitchat.data.mapper.UserMapper
import com.summitcodeworks.chitchat.data.remote.api.UserApiService
import com.summitcodeworks.chitchat.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OTP Authentication Repository
 * 
 * Handles SMS-based OTP authentication as the primary authentication method
 * for the ChitChat application.
 */
@Singleton
class OtpAuthRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val userDao: UserDao,
    private val otpAuthManager: OtpAuthManager,
    private val userMapper: UserMapper
) {

    /**
     * Sends OTP to the specified phone number
     */
    suspend fun sendOtp(phoneNumber: String): Result<Unit> {
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

    /**
     * Verifies OTP and authenticates the user
     */
    suspend fun verifyOtp(phoneNumber: String, otp: String): Result<OtpAuthResponse> {
        return try {
            val cleanedPhone = cleanPhoneNumber(phoneNumber)
            val request = VerifyOtpSmsRequest(phoneNumber = cleanedPhone, otp = otp)
            val response = userApiService.verifyOtp(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()?.data
                if (authResponse != null) {
                    // Save authentication data to OtpAuthManager
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
     * Gets the current user profile
     */
    suspend fun getUserProfile(): Result<UserDto> {
        return try {
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

    /**
     * Updates the user profile
     */
    suspend fun updateUserProfile(name: String, avatarUrl: String? = null, about: String? = null): Result<UserDto> {
        return try {
            val request = UpdateProfileRequest(
                name = name,
                avatarUrl = avatarUrl,
                about = about
            )

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

    /**
     * Syncs contacts with the server
     */
    suspend fun syncContacts(contacts: List<ContactDto>): Result<List<UserDto>> {
        return try {
            // Clean phone numbers in contacts before sending to API
            val cleanedContacts = contacts.map { contact ->
                contact.copy(phoneNumber = cleanPhoneNumber(contact.phoneNumber))
            }
            
            val request = ContactSyncRequest(contacts = cleanedContacts)
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

    /**
     * Blocks a user
     */
    suspend fun blockUser(userId: Long): Result<Unit> {
        return try {
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

    /**
     * Unblocks a user
     */
    suspend fun unblockUser(userId: Long): Result<Unit> {
        return try {
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

    /**
     * Updates online status
     */
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val response = userApiService.updateOnlineStatus(isOnline)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to update status"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the current user from OTP auth manager
     */
    fun getCurrentUser(): UserDto? = otpAuthManager.getCurrentUser()

    /**
     * Gets the current user ID
     */
    fun getCurrentUserId(): Long? = otpAuthManager.getCurrentUserId()

    /**
     * Gets the current user's phone number
     */
    fun getCurrentUserPhone(): String? = otpAuthManager.getCurrentUserPhone()

    /**
     * Gets the current access token
     */
    fun getCurrentToken(): String? = otpAuthManager.getCurrentToken()

    /**
     * Checks if the user is authenticated
     */
    fun isUserAuthenticated(): Boolean = otpAuthManager.isUserAuthenticated()

    /**
     * Signs out the current user
     */
    suspend fun signOut() {
        otpAuthManager.signOut()
        userDao.deleteAllUsers()
    }

    /**
     * Observes the current user
     */
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

    /**
     * Observes authentication state
     */
    fun observeAuthState(): Flow<Boolean> = otpAuthManager.isAuthenticated

    /**
     * Checks multiple phone numbers to see if they are registered
     */
    suspend fun checkMultiplePhoneNumbers(phoneNumbers: List<String>): Result<CheckPhonesResponse> {
        return try {
            // Clean and format all phone numbers before sending to API
            val cleanedPhoneNumbers = phoneNumbers.map { cleanPhoneNumber(it) }
            
            val request = CheckPhonesRequest(phoneNumbers = cleanedPhoneNumbers)
            val response = userApiService.checkMultiplePhoneNumbers(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val checkPhonesResponse = response.body()?.data
                if (checkPhonesResponse != null) {
                    Result.success(checkPhonesResponse)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to check phone numbers"
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

    /**
     * Gets user details by user ID from API and caches in local DB
     */
    suspend fun getUserById(userId: Long): Result<UserDto> {
        return try {
            val response = userApiService.getUserById(userId)

            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    // Save to local database
                    val userEntity = userMapper.dtoToEntity(userDto)
                    userDao.insertUser(userEntity)

                    Result.success(userDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get user details"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
