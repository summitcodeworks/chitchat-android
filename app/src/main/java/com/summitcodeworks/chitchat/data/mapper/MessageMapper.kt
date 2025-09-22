package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.MessageEntity
import com.summitcodeworks.chitchat.data.remote.dto.MessageDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageMapper @Inject constructor() {
    
    fun dtoToEntity(dto: MessageDto): MessageEntity {
        return MessageEntity(
            id = dto.id,
            senderId = dto.senderId,
            receiverId = dto.receiverId,
            groupId = dto.groupId,
            content = dto.content,
            messageType = dto.messageType,
            timestamp = dto.timestamp,
            isRead = dto.isRead,
            isDelivered = dto.isDelivered,
            replyToMessageId = dto.replyToMessageId,
            mediaId = dto.mediaId,
            isDeleted = dto.isDeleted,
            deleteForEveryone = dto.deleteForEveryone
        )
    }
    
    fun entityToDto(entity: MessageEntity): MessageDto {
        return MessageDto(
            id = entity.id,
            senderId = entity.senderId,
            receiverId = entity.receiverId,
            groupId = entity.groupId,
            content = entity.content,
            messageType = entity.messageType,
            timestamp = entity.timestamp,
            isRead = entity.isRead,
            isDelivered = entity.isDelivered,
            replyToMessageId = entity.replyToMessageId,
            mediaId = entity.mediaId,
            isDeleted = entity.isDeleted,
            deleteForEveryone = entity.deleteForEveryone
        )
    }
    
    fun entitiesToDtos(entities: List<MessageEntity>): List<MessageDto> {
        return entities.map { entityToDto(it) }
    }
    
    fun dtosToEntities(dtos: List<MessageDto>): List<MessageEntity> {
        return dtos.map { dtoToEntity(it) }
    }
}
