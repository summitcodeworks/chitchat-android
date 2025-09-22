package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE senderId = :senderId AND receiverId = :receiverId ORDER BY timestamp DESC")
    fun getConversationMessages(senderId: Long, receiverId: Long): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getGroupMessages(groupId: Long): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId OR receiverId = :userId) AND groupId IS NULL ORDER BY timestamp DESC")
    fun getAllUserMessages(userId: Long): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE receiverId = :userId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadMessages(userId: Long): Flow<List<MessageEntity>>
    
    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    suspend fun getUnreadMessageCount(userId: Long): Int
    
    @Query("SELECT * FROM messages WHERE senderId = :userId AND isDelivered = 0 ORDER BY timestamp ASC")
    suspend fun getUndeliveredMessages(userId: Long): List<MessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: String)
    
    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId")
    suspend fun markConversationAsRead(userId: Long, senderId: Long)
    
    @Query("UPDATE messages SET isDelivered = 1 WHERE id = :messageId")
    suspend fun markMessageAsDelivered(messageId: String)
    
    @Query("UPDATE messages SET isDeleted = 1, deleteForEveryone = :deleteForEveryone WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String, deleteForEveryone: Boolean = false)
    
    @Delete
    suspend fun deleteMessageEntity(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("DELETE FROM messages WHERE groupId = :groupId")
    suspend fun deleteGroupMessages(groupId: Long)
    
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}
