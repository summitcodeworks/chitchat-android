package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.MediaEntity
import com.summitcodeworks.chitchat.data.remote.dto.MediaDto
import com.summitcodeworks.chitchat.domain.model.Media
import com.summitcodeworks.chitchat.domain.model.MediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaMapper @Inject constructor() {
    
    fun toDomain(dto: MediaDto): Media {
        return Media(
            id = dto.id,
            fileName = dto.fileName,
            originalFileName = dto.originalFileName,
            fileSize = dto.fileSize,
            mediaType = MediaType.valueOf(dto.mediaType.uppercase()),
            mimeType = dto.mimeType,
            url = dto.url,
            thumbnailUrl = dto.thumbnailUrl,
            description = dto.description,
            uploadedBy = dto.uploadedBy,
            uploadedAt = dto.uploadedAt
        )
    }
    
    fun toDomain(entity: MediaEntity): Media {
        return Media(
            id = entity.id,
            fileName = entity.fileName,
            originalFileName = entity.originalFileName,
            fileSize = entity.fileSize,
            mediaType = MediaType.valueOf(entity.mediaType.uppercase()),
            mimeType = entity.mimeType,
            url = entity.url,
            thumbnailUrl = entity.thumbnailUrl,
            description = entity.description,
            uploadedBy = entity.uploadedBy,
            uploadedAt = entity.uploadedAt
        )
    }
    
    fun toEntity(domain: Media): MediaEntity {
        return MediaEntity(
            id = domain.id,
            fileName = domain.fileName,
            originalFileName = domain.originalFileName,
            fileSize = domain.fileSize,
            mediaType = domain.mediaType.name,
            mimeType = domain.mimeType,
            url = domain.url,
            thumbnailUrl = domain.thumbnailUrl,
            description = domain.description,
            uploadedBy = domain.uploadedBy,
            uploadedAt = domain.uploadedAt
        )
    }
    
    fun toEntity(dto: MediaDto): MediaEntity {
        return MediaEntity(
            id = dto.id,
            fileName = dto.fileName,
            originalFileName = dto.originalFileName,
            fileSize = dto.fileSize,
            mediaType = dto.mediaType,
            mimeType = dto.mimeType,
            url = dto.url,
            thumbnailUrl = dto.thumbnailUrl,
            description = dto.description,
            uploadedBy = dto.uploadedBy,
            uploadedAt = dto.uploadedAt
        )
    }
}
