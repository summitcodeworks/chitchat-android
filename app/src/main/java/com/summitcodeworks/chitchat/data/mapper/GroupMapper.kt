package com.summitcodeworks.chitchat.data.mapper

import com.summitcodeworks.chitchat.data.local.entity.GroupEntity
import com.summitcodeworks.chitchat.data.remote.dto.GroupDto
import com.summitcodeworks.chitchat.domain.model.Group
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupMapper @Inject constructor() {
    
    fun toDomain(dto: GroupDto): Group {
        return Group(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            groupPicture = dto.groupPicture,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            isActive = dto.isActive,
            members = dto.members?.map { memberDto ->
                com.summitcodeworks.chitchat.domain.model.GroupMember(
                    groupId = memberDto.groupId,
                    userId = memberDto.userId,
                    role = com.summitcodeworks.chitchat.domain.model.GroupRole.valueOf(memberDto.role.uppercase()),
                    joinedAt = memberDto.joinedAt,
                    user = memberDto.user?.let { userDto ->
                        com.summitcodeworks.chitchat.domain.model.User(
                            id = userDto.id,
                            phoneNumber = userDto.phoneNumber,
                            name = userDto.name,
                            avatarUrl = userDto.avatarUrl,
                            about = userDto.about,
                            lastSeen = userDto.lastSeen,
                            isOnline = userDto.isOnline,
                            createdAt = userDto.createdAt
                        )
                    }
                )
            }
        )
    }
    
    fun toDomain(entity: GroupEntity): Group {
        return Group(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            groupPicture = entity.groupPicture,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            isActive = entity.isActive,
            members = null // Members are stored separately in GroupMemberEntity
        )
    }
    
    fun toEntity(domain: Group): GroupEntity {
        return GroupEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            groupPicture = domain.groupPicture,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            isActive = domain.isActive
        )
    }
    
    fun toEntity(dto: GroupDto): GroupEntity {
        return GroupEntity(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            groupPicture = dto.groupPicture,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt,
            isActive = dto.isActive
        )
    }
}
