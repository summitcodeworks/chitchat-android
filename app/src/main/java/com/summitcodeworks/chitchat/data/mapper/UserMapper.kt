package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.UserEntity
import com.summitcodeworks.chitchat.data.remote.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMapper @Inject constructor() {
    
    fun dtoToEntity(dto: UserDto, isBlocked: Boolean = false, isContact: Boolean = false): UserEntity {
        return UserEntity(
            id = dto.id,
            phoneNumber = dto.phoneNumber,
            name = dto.name,
            avatarUrl = dto.avatarUrl,
            about = dto.about,
            lastSeen = dto.lastSeen,
            isOnline = dto.isOnline,
            createdAt = dto.createdAt,
            isBlocked = isBlocked,
            isContact = isContact
        )
    }
    
    fun entityToDto(entity: UserEntity): UserDto {
        return UserDto(
            id = entity.id,
            phoneNumber = entity.phoneNumber,
            name = entity.name,
            avatarUrl = entity.avatarUrl,
            about = entity.about,
            lastSeen = entity.lastSeen,
            isOnline = entity.isOnline,
            createdAt = entity.createdAt
        )
    }
    
    fun entitiesToDtos(entities: List<UserEntity>): List<UserDto> {
        return entities.map { entityToDto(it) }
    }
    
    fun dtosToEntities(dtos: List<UserDto>, isBlocked: Boolean = false, isContact: Boolean = false): List<UserEntity> {
        return dtos.map { dtoToEntity(it, isBlocked, isContact) }
    }
}
