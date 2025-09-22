package com.summitcodeworks.chitchat.domain.usecase.call

import com.summitcodeworks.chitchat.data.repository.CallRepository
import com.summitcodeworks.chitchat.domain.model.Call
import com.summitcodeworks.chitchat.domain.model.CallType
import javax.inject.Inject

class InitiateCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    
    suspend operator fun invoke(
        token: String,
        calleeId: Long? = null,
        groupId: Long? = null,
        callType: CallType
    ): Result<Call> {
        return try {
            val request = com.summitcodeworks.chitchat.data.remote.dto.InitiateCallRequest(
                calleeId = calleeId,
                groupId = groupId,
                callType = callType.name
            )
            
            val result = callRepository.initiateCall(token, request)
            
            result.fold(
                onSuccess = { callDto ->
                    val call = Call(
                        sessionId = callDto.sessionId,
                        callerId = callDto.callerId,
                        calleeId = callDto.calleeId,
                        callType = CallType.valueOf(callDto.callType),
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
                    Result.success(call)
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
