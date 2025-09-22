package com.summitcodeworks.chitchat.domain.usecase.message

import com.summitcodeworks.chitchat.data.repository.MessageRepository
import com.summitcodeworks.chitchat.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGroupMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(
        token: String,
        groupId: Long,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Message>> {
        return try {
            val result = messageRepository.getGroupMessages(token, groupId, page, size)
            
            result.fold(
                onSuccess = { pageResponse ->
                    val messages = pageResponse.content.map { messageDto ->
                        Message(
                            id = messageDto.id,
                            senderId = messageDto.senderId,
                            receiverId = messageDto.receiverId,
                            groupId = messageDto.groupId,
                            content = messageDto.content,
                            messageType = com.summitcodeworks.chitchat.domain.model.MessageType.valueOf(messageDto.messageType),
                            timestamp = messageDto.timestamp,
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
                    }
                    Result.success(messages)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeLocalGroupMessages(groupId: Long): Flow<List<Message>> {
        return messageRepository.getLocalGroupMessages(groupId)
            .map { entities ->
                entities.map { entity ->
                    Message(
                        id = entity.id,
                        senderId = entity.senderId,
                        receiverId = entity.receiverId,
                        groupId = entity.groupId,
                        content = entity.content,
                        messageType = com.summitcodeworks.chitchat.domain.model.MessageType.valueOf(entity.messageType),
                        timestamp = entity.timestamp,
                        isRead = entity.isRead,
                        isDelivered = entity.isDelivered,
                        replyToMessageId = entity.replyToMessageId,
                        mediaId = entity.mediaId,
                        isDeleted = entity.isDeleted,
                        deleteForEveryone = entity.deleteForEveryone
                    )
                }
            }
    }
}
