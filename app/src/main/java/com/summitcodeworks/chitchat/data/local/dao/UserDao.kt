package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    suspend fun getUserByPhoneNumber(phoneNumber: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE isContact = 1 ORDER BY name ASC")
    fun getContacts(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE isBlocked = 1 ORDER BY name ASC")
    fun getBlockedUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users WHERE id IN (:userIds) ORDER BY name ASC")
    suspend fun getUsersByIds(userIds: List<Long>): List<UserEntity>
    
    @Query("SELECT * FROM users WHERE phoneNumber IN (:phoneNumbers) ORDER BY name ASC")
    suspend fun getUsersByPhoneNumbers(phoneNumbers: List<String>): List<UserEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, isOnline: Boolean, lastSeen: String?)
    
    @Query("UPDATE users SET isBlocked = :isBlocked WHERE id = :userId")
    suspend fun updateUserBlockStatus(userId: Long, isBlocked: Boolean)
    
    @Query("UPDATE users SET isContact = :isContact WHERE id = :userId")
    suspend fun updateUserContactStatus(userId: Long, isContact: Boolean)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
