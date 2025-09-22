package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.StatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    
    @Query("SELECT * FROM statuses WHERE id = :statusId")
    suspend fun getStatusById(statusId: Long): StatusEntity?
    
    @Query("SELECT * FROM statuses WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getUserStatuses(userId: Long): List<StatusEntity>
    
    @Query("SELECT * FROM statuses WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserStatusesFlow(userId: Long): Flow<List<StatusEntity>>
    
    @Query("SELECT * FROM statuses WHERE userId IN (:userIds) AND expiresAt > datetime('now') ORDER BY createdAt DESC")
    fun getActiveStatuses(userIds: List<Long>): Flow<List<StatusEntity>>
    
    @Query("SELECT * FROM statuses WHERE expiresAt > datetime('now') ORDER BY createdAt DESC")
    fun getActiveStatusesFlow(): Flow<List<StatusEntity>>
    
    @Query("SELECT * FROM statuses WHERE expiresAt > datetime('now') ORDER BY createdAt DESC")
    suspend fun getActiveStatuses(): List<StatusEntity>
    
    @Query("SELECT * FROM statuses WHERE userId = :userId AND expiresAt > datetime('now') ORDER BY createdAt DESC")
    fun getActiveUserStatuses(userId: Long): Flow<List<StatusEntity>>
    
    @Query("SELECT * FROM statuses WHERE expiresAt < datetime('now')")
    suspend fun getExpiredStatuses(): List<StatusEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: StatusEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatuses(statuses: List<StatusEntity>)
    
    @Update
    suspend fun updateStatus(status: StatusEntity)
    
    @Query("UPDATE statuses SET viewCount = viewCount + 1 WHERE id = :statusId")
    suspend fun incrementViewCount(statusId: Long)
    
    @Query("UPDATE statuses SET reactionCount = reactionCount + 1 WHERE id = :statusId")
    suspend fun incrementReactionCount(statusId: Long)
    
    @Query("UPDATE statuses SET reactionCount = reactionCount - 1 WHERE id = :statusId")
    suspend fun decrementReactionCount(statusId: Long)
    
    @Delete
    suspend fun deleteStatus(status: StatusEntity)
    
    @Query("DELETE FROM statuses WHERE id = :statusId")
    suspend fun deleteStatusById(statusId: Long)
    
    @Query("DELETE FROM statuses WHERE userId = :userId")
    suspend fun deleteUserStatuses(userId: Long)
    
    @Query("DELETE FROM statuses WHERE expiresAt < datetime('now')")
    suspend fun deleteExpiredStatuses()
    
    @Query("DELETE FROM statuses")
    suspend fun deleteAllStatuses()
}
