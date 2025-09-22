package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.GroupEntity
import com.summitcodeworks.chitchat.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): GroupEntity?
    
    @Query("SELECT * FROM groups WHERE id IN (SELECT groupId FROM group_members WHERE userId = :userId AND role != 'LEFT') ORDER BY createdAt DESC")
    fun getUserGroups(userId: Long): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveGroups(): Flow<List<GroupEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)
    
    @Update
    suspend fun updateGroup(group: GroupEntity)
    
    @Query("UPDATE groups SET name = :name, description = :description WHERE id = :groupId")
    suspend fun updateGroupInfo(groupId: Long, name: String, description: String?)
    
    @Query("UPDATE groups SET groupPicture = :groupPicture WHERE id = :groupId")
    suspend fun updateGroupPicture(groupId: Long, groupPicture: String)
    
    @Query("UPDATE groups SET isActive = 0 WHERE id = :groupId")
    suspend fun deactivateGroup(groupId: Long)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)
    
    @Query("DELETE FROM groups")
    suspend fun deleteAllGroups()
}

@Dao
interface GroupMemberDao {
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getGroupMembers(groupId: Long): List<GroupMemberEntity>
    
    @Query("SELECT * FROM group_members WHERE userId = :userId")
    suspend fun getUserGroupMemberships(userId: Long): List<GroupMemberEntity>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun getGroupMember(groupId: Long, userId: Long): GroupMemberEntity?
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND role = 'ADMIN'")
    suspend fun getGroupAdmins(groupId: Long): List<GroupMemberEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMemberEntity>)
    
    @Update
    suspend fun updateGroupMember(member: GroupMemberEntity)
    
    @Query("UPDATE group_members SET role = :role WHERE groupId = :groupId AND userId = :userId")
    suspend fun updateMemberRole(groupId: Long, userId: Long, role: String)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeGroupMember(groupId: Long, userId: Long)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteGroupMembers(groupId: Long)
    
    @Query("DELETE FROM group_members WHERE userId = :userId")
    suspend fun deleteUserGroupMemberships(userId: Long)
    
    @Query("DELETE FROM group_members")
    suspend fun deleteAllGroupMembers()
}
