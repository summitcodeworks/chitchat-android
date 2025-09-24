package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.local.dao.MessageDao
import com.summitcodeworks.chitchat.data.local.entity.MessageEntity
import com.summitcodeworks.chitchat.data.mapper.MessageMapper
import com.summitcodeworks.chitchat.data.remote.api.MessageApiService
import com.summitcodeworks.chitchat.data.remote.dto.*
import com.summitcodeworks.chitchat.data.remote.websocket.ChitChatWebSocketClient
import com.summitcodeworks.chitchat.data.remote.websocket.WebSocketMessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageApiService: MessageApiService,
    private val messageDao: MessageDao,
    private val messageMapper: MessageMapper,
    private val webSocketClient: ChitChatWebSocketClient
) {
    
    suspend fun sendMessage(token: String, request: SendMessageRequest): Result<MessageDto> {
        return try {
            val response = messageApiService.sendMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val messageDto = response.body()?.data
                if (messageDto != null) {
                    // Save to local database
                    val messageEntity = messageMapper.dtoToEntity(messageDto)
                    messageDao.insertMessage(messageEntity)
                    
                    Result.success(messageDto)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to send message"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getConversationMessages(token: String, userId: Long, page: Int = 0, size: Int = 20): Result<MessagePageResponse> {
        return try {
            val response = messageApiService.getConversationMessages(
                userId, page, size
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val pageResponse = response.body()?.data
                if (pageResponse != null) {
                    // Save messages to local database
                    val messageEntities = pageResponse.content.map { messageMapper.dtoToEntity(it) }
                    messageDao.insertMessages(messageEntities)
                    
                    Result.success(pageResponse)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get messages"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getGroupMessages(token: String, groupId: Long, page: Int = 0, size: Int = 20): Result<MessagePageResponse> {
        return try {
            val response = messageApiService.getGroupMessages(
                groupId, page, size
            )
            
            if (response.isSuccessful && response.body()?.success == true) {
                val pageResponse = response.body()?.data
                if (pageResponse != null) {
                    // Save messages to local database
                    val messageEntities = pageResponse.content.map { messageMapper.dtoToEntity(it) }
                    messageDao.insertMessages(messageEntities)
                    
                    Result.success(pageResponse)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to get group messages"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchMessages(token: String, query: String): Result<List<MessageDto>> {
        return try {
            val response = messageApiService.searchMessages(query)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val messages = response.body()?.data
                if (messages != null) {
                    Result.success(messages)
                } else {
                    Result.failure(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Failed to search messages"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markMessageAsRead(token: String, messageId: String): Result<Unit> {
        return try {
            val response = messageApiService.markMessageAsRead(messageId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database
                messageDao.markMessageAsRead(messageId)
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to mark message as read"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMessage(token: String, messageId: String, deleteForEveryone: Boolean = false): Result<Unit> {
        return try {
            val response = messageApiService.deleteMessage(messageId, deleteForEveryone)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local database
                messageDao.deleteMessage(messageId, deleteForEveryone)
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to delete message"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Local database operations
    fun getLocalConversationMessages(senderId: Long, receiverId: Long): Flow<List<MessageEntity>> {
        return messageDao.getConversationMessages(senderId, receiverId)
    }
    
    fun getLocalGroupMessages(groupId: Long): Flow<List<MessageEntity>> {
        return messageDao.getGroupMessages(groupId)
    }
    
    fun getLocalUnreadMessages(userId: Long): Flow<List<MessageEntity>> {
        return messageDao.getUnreadMessages(userId)
    }
    
    suspend fun getLocalUnreadMessageCount(userId: Long): Int {
        return messageDao.getUnreadMessageCount(userId)
    }
    
    suspend fun insertLocalMessage(messageEntity: MessageEntity) {
        messageDao.insertMessage(messageEntity)
    }
    
    suspend fun insertLocalMessages(messageEntities: List<MessageEntity>) {
        messageDao.insertMessages(messageEntities)
    }
    
    // WebSocket operations
    fun sendTypingIndicator(receiverId: Long, isTyping: Boolean) {
        webSocketClient.sendTypingIndicator(receiverId, isTyping)
    }
    
    fun observeWebSocketMessages() = webSocketClient.messages
    
    fun observeConnectionState() = webSocketClient.connectionState
}
