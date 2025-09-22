package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.StatusEntity
import com.summitcodeworks.chitchat.data.remote.dto.StatusDto
import com.summitcodeworks.chitchat.domain.model.Status
import com.summitcodeworks.chitchat.domain.model.StatusType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusMapper @Inject constructor() {
    
    fun toDomain(dto: StatusDto): Status {
        return Status(
            id = dto.id,
            userId = dto.userId,
            content = dto.content,
            statusType = StatusType.valueOf(dto.statusType.uppercase()),
            mediaId = dto.mediaId,
            backgroundColor = dto.backgroundColor,
            font = dto.font,
            privacy = com.summitcodeworks.chitchat.domain.model.Privacy.valueOf(dto.privacy.uppercase()),
            viewCount = dto.viewCount,
            reactionCount = dto.reactionCount,
            createdAt = dto.createdAt,
            expiresAt = dto.expiresAt,
            user = dto.user?.let { userDto ->
                com.summitcodeworks.chitchat.domain.model.User(
                    id = userDto.id,
                    phoneNumber = userDto.phoneNumber,
                    name = userDto.name,
                    avatarUrl = userDto.avatarUrl,
                    about = userDto.about,
                    isOnline = userDto.isOnline,
                    lastSeen = userDto.lastSeen
                )
            }
        )
    }
    
    fun toDomain(entity: StatusEntity): Status {
        return Status(
            id = entity.id,
            userId = entity.userId,
            content = entity.content,
            statusType = StatusType.valueOf(entity.statusType.uppercase()),
            mediaId = entity.mediaId,
            backgroundColor = entity.backgroundColor,
            font = entity.font,
            privacy = com.summitcodeworks.chitchat.domain.model.Privacy.valueOf(entity.privacy.uppercase()),
            viewCount = entity.viewCount,
            reactionCount = entity.reactionCount,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
            user = null // User info not stored in status entity
        )
    }
    
    fun toEntity(domain: Status): StatusEntity {
        return StatusEntity(
            id = domain.id,
            userId = domain.userId,
            content = domain.content,
            statusType = domain.statusType.name,
            mediaId = domain.mediaId,
            backgroundColor = domain.backgroundColor,
            font = domain.font,
            privacy = domain.privacy.name,
            viewCount = domain.viewCount,
            reactionCount = domain.reactionCount,
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt
        )
    }
    
    fun toEntity(dto: StatusDto): StatusEntity {
        return StatusEntity(
            id = dto.id,
            userId = dto.userId,
            content = dto.content,
            statusType = dto.statusType,
            mediaId = dto.mediaId,
            backgroundColor = dto.backgroundColor,
            font = dto.font,
            privacy = dto.privacy,
            viewCount = dto.viewCount,
            reactionCount = dto.reactionCount,
            createdAt = dto.createdAt,
            expiresAt = dto.expiresAt
        )
    }
}
