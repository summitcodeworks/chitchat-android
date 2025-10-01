package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for message-related database operations in ChitChat.
 * 
 * This interface defines all database operations for managing chat messages
 * in the local Room database. It provides methods for CRUD operations,
 * querying conversations, searching messages, and managing message status.
 * 
 * Key functionalities:
 * - Message storage and retrieval
 * - Conversation message queries
 * - Message search and filtering
 * - Read receipt and delivery status management
 * - Unread message counting
 * - Message deletion and cleanup
 * 
 * Database operations supported:
 * - Insert/Update/Delete individual messages
 * - Bulk operations for message synchronization
 * - Complex queries for conversation history
 * - Search functionality across message content
 * - Status updates for read/delivered states
 * 
 * Performance optimizations:
 * - Indexed queries for fast conversation loading
 * - Pagination support for large message histories
 * - Efficient unread count calculations
 * - Optimized search queries with LIKE operations
 * 
 * The DAO supports both direct messages and group messages through
 * conditional queries based on receiverId and groupId parameters.
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE senderId = :senderId AND receiverId = :receiverId ORDER BY timestamp ASC")
    fun getConversationMessages(senderId: Long, receiverId: Long): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupMessages(groupId: Long): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE (senderId = :userId OR receiverId = :userId) AND groupId IS NULL ORDER BY timestamp ASC")
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
