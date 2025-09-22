package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.NotificationEntity
import com.summitcodeworks.chitchat.data.remote.dto.NotificationDto
import com.summitcodeworks.chitchat.domain.model.Notification
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationMapper @Inject constructor() {
    
    fun toDomain(dto: NotificationDto): Notification {
        return Notification(
            id = dto.id,
            userId = dto.userId,
            type = dto.type,
            title = dto.title,
            body = dto.body,
            data = dto.data,
            isRead = dto.isRead,
            createdAt = dto.createdAt,
            readAt = dto.readAt
        )
    }
    
    fun toDomain(entity: NotificationEntity): Notification {
        return Notification(
            id = entity.id,
            userId = entity.userId,
            type = entity.type,
            title = entity.title,
            body = entity.body,
            data = entity.data,
            isRead = entity.isRead,
            createdAt = entity.createdAt,
            readAt = entity.readAt
        )
    }
    
    fun toEntity(domain: Notification): NotificationEntity {
        return NotificationEntity(
            id = domain.id,
            userId = domain.userId,
            type = domain.type,
            title = domain.title,
            body = domain.body,
            data = domain.data,
            isRead = domain.isRead,
            createdAt = domain.createdAt,
            readAt = domain.readAt
        )
    }
    
    fun toEntity(dto: NotificationDto): NotificationEntity {
        return NotificationEntity(
            id = dto.id,
            userId = dto.userId,
            type = dto.type,
            title = dto.title,
            body = dto.body,
            data = dto.data,
            isRead = dto.isRead,
            createdAt = dto.createdAt,
            readAt = dto.readAt
        )
    }
}
