package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.local.dao.CallDao
import com.summitcodeworks.chitchat.data.local.entity.CallEntity
import com.summitcodeworks.chitchat.data.mapper.CallMapper
import com.summitcodeworks.chitchat.data.remote.api.CallApiService
import com.summitcodeworks.chitchat.data.remote.dto.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    private val callApiService: CallApiService,
    private val callDao: CallDao,
    private val callMapper: CallMapper
) {
    
    suspend fun initiateCall(token: String, request: InitiateCallRequest): Result<CallDto> {
        return try {
            val response = callApiService.initiateCall("Bearer $token", request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val callDto = response.body()?.data
                if (callDto != null) {
                    // Save to local database
                    val callEntity = callMapper.dtoToEntity(callDto)
                    callDao.insertCall(callEntity)
                    
                    Result.success(callDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to initiate call"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun answerCall(token: String, sessionId: String, accepted: Boolean, sdpAnswer: String? = null): Result<CallDto> {
        return try {
            val request = AnswerCallRequest(
                accepted = accepted,
                sdpAnswer = sdpAnswer
            )
            
            val response = callApiService.answerCall("Bearer $token", sessionId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val callDto = response.body()?.data
                if (callDto != null) {
                    // Update local database
                    val callEntity = callMapper.dtoToEntity(callDto)
                    callDao.updateCall(callEntity)
                    
                    Result.success(callDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to answer call"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectCall(token: String, sessionId: String, reason: String? = null): Result<CallDto> {
        return try {
            val response = callApiService.rejectCall("Bearer $token", sessionId, reason)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val callDto = response.body()?.data
                if (callDto != null) {
                    // Update local database
                    val callEntity = callMapper.dtoToEntity(callDto)
                    callDao.updateCall(callEntity)
                    
                    Result.success(callDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to reject call"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun endCall(token: String, sessionId: String, reason: String? = null): Result<CallDto> {
        return try {
            val response = callApiService.endCall("Bearer $token", sessionId, reason)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val callDto = response.body()?.data
                if (callDto != null) {
                    // Update local database
                    val callEntity = callMapper.dtoToEntity(callDto)
                    callDao.updateCall(callEntity)
                    
                    Result.success(callDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to end call"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCallHistory(token: String, page: Int = 0, size: Int = 20): Result<CallPageResponse> {
        return try {
            val response = callApiService.getCallHistory("Bearer $token", page, size)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val pageResponse = response.body()?.data
                if (pageResponse != null) {
                    // Save calls to local database
                    val callEntities = pageResponse.content.map { callMapper.dtoToEntity(it) }
                    callDao.insertCalls(callEntities)
                    
                    Result.success(pageResponse)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get call history"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMissedCalls(token: String): Result<List<CallDto>> {
        return try {
            val response = callApiService.getMissedCalls("Bearer $token")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val calls = response.body()?.data
                if (calls != null) {
                    Result.success(calls)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get missed calls"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getRecentCalls(token: String, limit: Int = 10): Result<List<CallDto>> {
        return try {
            val response = callApiService.getRecentCalls("Bearer $token", limit)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val calls = response.body()?.data
                if (calls != null) {
                    Result.success(calls)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get recent calls"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Local database operations
    fun getLocalCallHistory(userId: Long): Flow<List<CallEntity>> {
        return callDao.getUserCallHistory(userId)
    }
    
    fun getLocalMissedCalls(userId: Long): Flow<List<CallEntity>> {
        return callDao.getMissedCalls(userId)
    }
    
    suspend fun getLocalRecentCalls(userId: Long, limit: Int = 10): List<CallEntity> {
        return callDao.getRecentCalls(userId, limit)
    }
    
    suspend fun insertLocalCall(callEntity: CallEntity) {
        callDao.insertCall(callEntity)
    }
    
    suspend fun updateLocalCall(callEntity: CallEntity) {
        callDao.updateCall(callEntity)
    }
}
