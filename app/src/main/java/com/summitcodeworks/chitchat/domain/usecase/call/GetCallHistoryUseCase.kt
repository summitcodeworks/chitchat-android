package com.summitcodeworks.chitchat.domain.usecase.call

import com.summitcodeworks.chitchat.data.repository.CallRepository
import com.summitcodeworks.chitchat.domain.model.Call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(
        token: String,
        page: Int = 0,
        size: Int = 20
    ): Result<List<Call>> {
        return try {
            val result = callRepository.getCallHistory(token, page, size)
            result.fold(
                onSuccess = { pageResponse ->
                    val calls = pageResponse.content.map { callDto ->
                        Call(
                            sessionId = callDto.sessionId,
                            callerId = callDto.callerId,
                            calleeId = callDto.calleeId,
                            callType = com.summitcodeworks.chitchat.domain.model.CallType.valueOf(callDto.callType),
                            status = com.summitcodeworks.chitchat.domain.model.CallStatus.valueOf(callDto.status),
                            startTime = callDto.startTime,
                            endTime = callDto.endTime,
                            duration = callDto.duration,
                            groupId = callDto.groupId,
                            caller = callDto.caller?.let { 
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
                            callee = callDto.callee?.let {
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
                            }
                        )
                    }
                    Result.success(calls)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getLocalCallHistory(userId: Long): Flow<List<Call>> {
        return callRepository.getLocalCallHistory(userId).map { entities ->
            entities.map { entity ->
                Call(
                    sessionId = entity.sessionId,
                    callerId = entity.callerId,
                    calleeId = entity.calleeId,
                    callType = com.summitcodeworks.chitchat.domain.model.CallType.valueOf(entity.callType),
                    status = com.summitcodeworks.chitchat.domain.model.CallStatus.valueOf(entity.status),
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    duration = entity.duration,
                    groupId = entity.groupId
                )
            }
        }
    }
}
