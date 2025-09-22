package com.summitcodeworks.chitchat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import android.app.Activity
import java.util.concurrent.TimeUnit
import com.summitcodeworks.chitchat.data.local.dao.UserDao
import com.summitcodeworks.chitchat.data.local.entity.UserEntity
import com.summitcodeworks.chitchat.data.mapper.UserMapper
import com.summitcodeworks.chitchat.data.remote.api.UserApiService
import com.summitcodeworks.chitchat.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val userMapper: UserMapper
) {

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhoneNumber(phoneNumber: String, verificationId: String, code: String): Result<FirebaseUser> {
        return try {
            val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId, code)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user
            
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Firebase authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun authenticateWithBackend(idToken: String, name: String? = null, deviceInfo: String? = null): Result<FirebaseAuthResponse> {
        return try {
            val request = FirebaseAuthRequest(
                idToken = idToken,
                name = name,
                deviceInfo = deviceInfo
            )
            
            val response = userApiService.authenticateWithFirebase(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val authResponse = response.body()?.data
                if (authResponse != null) {
                    // Save user to local database
                    val userEntity = userMapper.dtoToEntity(authResponse.user)
                    userDao.insertUser(userEntity)
                    
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Authentication failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(token: String): Result<UserDto> {
        return try {
            val response = userApiService.getUserProfile("Bearer $token")
            
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
    
    suspend fun updateUserProfile(token: String, name: String, avatarUrl: String? = null, about: String? = null): Result<UserDto> {
        return try {
            val request = UpdateProfileRequest(
                name = name,
                avatarUrl = avatarUrl,
                about = about
            )
            
            val response = userApiService.updateUserProfile("Bearer $token", request)
            
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
    
    suspend fun syncContacts(token: String, contacts: List<ContactDto>): Result<List<UserDto>> {
        return try {
            val request = ContactSyncRequest(contacts = contacts)
            val response = userApiService.syncContacts("Bearer $token", request)
            
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
    
    suspend fun blockUser(token: String, userId: Long): Result<Unit> {
        return try {
            val response = userApiService.blockUser("Bearer $token", userId)
            
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
    
    suspend fun unblockUser(token: String, userId: Long): Result<Unit> {
        return try {
            val response = userApiService.unblockUser("Bearer $token", userId)
            
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
    
    suspend fun updateOnlineStatus(token: String, isOnline: Boolean): Result<Unit> {
        return try {
            val response = userApiService.updateOnlineStatus("Bearer $token", isOnline)
            
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
    
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    suspend fun getCurrentUserToken(): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        userDao.deleteAllUsers()
    }
    
    fun observeCurrentUser(): Flow<UserEntity?> {
        val currentUserId = getCurrentUserId()?.toLongOrNull()
        return if (currentUserId != null) {
            flow {
                val user = userDao.getUserById(currentUserId)
                emit(user)
            }
        } else {
            flow { emit(null) }
        }
    }
}
