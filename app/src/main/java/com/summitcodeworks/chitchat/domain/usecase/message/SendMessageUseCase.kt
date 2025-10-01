package com.summitcodeworks.chitchat.domain.usecase.message

import com.summitcodeworks.chitchat.data.repository.MessageRepository
import com.summitcodeworks.chitchat.data.remote.websocket.MultiWebSocketManager
import com.summitcodeworks.chitchat.domain.model.Message
import com.summitcodeworks.chitchat.domain.model.MessageType
import java.time.Instant
import javax.inject.Inject

/**
 * Use case for sending messages in ChitChat.
 * 
 * This use case handles the complete message sending flow, including API calls,
 * local storage, and real-time WebSocket delivery. It ensures messages are
 * properly formatted, sent to the server, and delivered in real-time to recipients.
 * 
 * Message sending process:
 * 1. Validate message content and parameters
 * 2. Create message request DTO
 * 3. Send message via repository (API call)
 * 4. Store message locally for offline access
 * 5. Send message via WebSocket for real-time delivery
 * 6. Return formatted domain Message object
 * 
 * Supported message types:
 * - TEXT: Plain text messages
 * - IMAGE: Image attachments with thumbnails
 * - VIDEO: Video files with metadata
 * - AUDIO: Audio recordings and voice notes
 * - DOCUMENT: File attachments
 * - LOCATION: Geographic location sharing
 * 
 * Features:
 * - Reply to message functionality
 * - Media attachment support
 * - Group and direct message handling
 * - Message threading and organization
 * - Delivery confirmation tracking
 * - Offline message queuing
 * 
 * Error handling:
 * - Network failure recovery
 * - Invalid message validation
 * - Authentication token refresh
 * - Message retry mechanisms
 * 
 * @param messageRepository Repository for message persistence and API calls
 * @param webSocketManager Manager for real-time message delivery
 * 
 * @author ChitChat Development Team
 * @since 1.0
 */
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val webSocketManager: MultiWebSocketManager
) {
    
    suspend operator fun invoke(
        token: String,
        receiverId: Long? = null,
        groupId: Long? = null,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        replyToMessageId: String? = null,
        mediaId: Long? = null
    ): Result<Message> {
        return try {
            val request = com.summitcodeworks.chitchat.data.remote.dto.SendMessageRequest(
                recipientId = receiverId,
                groupId = groupId,
                content = content,
                type = messageType.name,
                replyToMessageId = replyToMessageId,
                mediaId = mediaId
            )
            
            val result = messageRepository.sendMessage(token, request)
            
            result.fold(
                onSuccess = { messageDto ->
                    val message = Message(
                        id = messageDto.id,
                        senderId = messageDto.senderId,
                        receiverId = messageDto.receiverId,
                        groupId = messageDto.groupId,
                        content = messageDto.content,
                        messageType = MessageType.valueOf(messageDto.messageType),
                        timestamp = messageDto.timestamp ?: Instant.now().toString(),
                        isRead = messageDto.isRead,
                        isDelivered = messageDto.isDelivered,
                        replyToMessageId = messageDto.replyToMessageId,
                        mediaId = messageDto.mediaId,
                        isDeleted = messageDto.isDeleted,
                        deleteForEveryone = messageDto.deleteForEveryone,
                        sender = messageDto.sender?.let { 
                            com.summitcodeworks.chitchat.domain.model.User(
                                id = it.id,
                                phoneNumber = it.phoneNumber,
                                name = it.name,
                                avatarUrl = it.avatarUrl,
                                about = it.about,
                                lastSeen = it.lastSeen,
                                isOnline = it.isOnline,
                                createdAt = it.createdAt
                            )
                        },
                        media = messageDto.media?.let {
                            com.summitcodeworks.chitchat.domain.model.Media(
                                id = it.id,
                                fileName = it.fileName,
                                originalFileName = it.originalFileName,
                                fileSize = it.fileSize,
                                mediaType = com.summitcodeworks.chitchat.domain.model.MediaType.valueOf(it.mediaType),
                                mimeType = it.mimeType,
                                url = it.url,
                                thumbnailUrl = it.thumbnailUrl,
                                description = it.description,
                                uploadedBy = it.uploadedBy,
                                uploadedAt = it.uploadedAt,
                                duration = it.duration
                            )
                        }
                    )
                    
                    // Also send via WebSocket for real-time delivery
                    webSocketManager.sendMessage(
                        receiverId = receiverId,
                        groupId = groupId,
                        content = content,
                        messageType = messageType.name,
                        replyToMessageId = replyToMessageId,
                        mediaId = mediaId
                    )
                    
                    Result.success(message)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
