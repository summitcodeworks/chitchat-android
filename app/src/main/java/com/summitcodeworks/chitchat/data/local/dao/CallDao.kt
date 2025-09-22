package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    
    @Query("SELECT * FROM calls WHERE sessionId = :sessionId")
    suspend fun getCallBySessionId(sessionId: String): CallEntity?
    
    @Query("SELECT * FROM calls WHERE callerId = :userId OR calleeId = :userId ORDER BY startTime DESC")
    fun getUserCallHistory(userId: Long): Flow<List<CallEntity>>
    
    @Query("SELECT * FROM calls WHERE calleeId = :userId AND status = 'MISSED' ORDER BY startTime DESC")
    fun getMissedCalls(userId: Long): Flow<List<CallEntity>>
    
    @Query("SELECT * FROM calls WHERE callerId = :userId OR calleeId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentCalls(userId: Long, limit: Int = 10): List<CallEntity>
    
    @Query("SELECT * FROM calls WHERE groupId = :groupId ORDER BY startTime DESC")
    fun getGroupCallHistory(groupId: Long): Flow<List<CallEntity>>
    
    @Query("SELECT COUNT(*) FROM calls WHERE calleeId = :userId AND status = 'MISSED' AND startTime > :since")
    suspend fun getMissedCallCountSince(userId: Long, since: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallEntity>)
    
    @Update
    suspend fun updateCall(call: CallEntity)
    
    @Query("UPDATE calls SET status = :status, endTime = :endTime, duration = :duration WHERE sessionId = :sessionId")
    suspend fun updateCallStatus(sessionId: String, status: String, endTime: String?, duration: Long?)
    
    @Delete
    suspend fun deleteCall(call: CallEntity)
    
    @Query("DELETE FROM calls WHERE sessionId = :sessionId")
    suspend fun deleteCallBySessionId(sessionId: String)
    
    @Query("DELETE FROM calls WHERE callerId = :userId OR calleeId = :userId")
    suspend fun deleteUserCalls(userId: Long)
    
    @Query("DELETE FROM calls")
    suspend fun deleteAllCalls()
}
