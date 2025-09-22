package com.summitcodeworks.chitchat.domain.usecase.call

import com.summitcodeworks.chitchat.data.repository.CallRepository
import com.summitcodeworks.chitchat.domain.model.Call
import javax.inject.Inject

class RejectCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(
        token: String,
        sessionId: String,
        reason: String? = null
    ): Result<Call> {
        return try {
            val result = callRepository.rejectCall(token, sessionId, reason)
            result.fold(
                onSuccess = { callDto ->
                    val call = Call(
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
