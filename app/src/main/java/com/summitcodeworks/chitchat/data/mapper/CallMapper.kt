package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.CallEntity
import com.summitcodeworks.chitchat.data.remote.dto.CallDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallMapper @Inject constructor() {
    
    fun dtoToEntity(dto: CallDto): CallEntity {
        return CallEntity(
            sessionId = dto.sessionId,
            callerId = dto.callerId,
            calleeId = dto.calleeId,
            callType = dto.callType,
            status = dto.status,
            startTime = dto.startTime,
            endTime = dto.endTime,
            duration = dto.duration,
            groupId = dto.groupId
        )
    }
    
    fun entityToDto(entity: CallEntity): CallDto {
        return CallDto(
            sessionId = entity.sessionId,
            callerId = entity.callerId,
            calleeId = entity.calleeId,
            callType = entity.callType,
            status = entity.status,
            startTime = entity.startTime,
            endTime = entity.endTime,
            duration = entity.duration,
            groupId = entity.groupId
        )
    }
    
    fun entitiesToDtos(entities: List<CallEntity>): List<CallDto> {
        return entities.map { entityToDto(it) }
    }
    
    fun dtosToEntities(dtos: List<CallDto>): List<CallEntity> {
        return dtos.map { dtoToEntity(it) }
    }
}
