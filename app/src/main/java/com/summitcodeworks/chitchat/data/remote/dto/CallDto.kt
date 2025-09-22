package com.summitcodeworks.chitchat.data.remote.dto

data class CallDto(
    val sessionId: String,
    val callerId: Long,
    val calleeId: Long,
    val callType: String, // VOICE, VIDEO
    val status: String, // INITIATED, RINGING, ANSWERED, REJECTED, ENDED, MISSED
    val startTime: String,
    val endTime: String? = null,
    val duration: Long? = null, // in seconds
    val groupId: Long? = null,
    val caller: UserDto? = null,
    val callee: UserDto? = null
)

data class InitiateCallRequest(
    val calleeId: Long? = null,
    val groupId: Long? = null,
    val callType: String // VOICE, VIDEO
)

data class AnswerCallRequest(
    val accepted: Boolean,
    val sdpAnswer: String? = null
)

data class RejectCallRequest(
    val reason: String? = null // BUSY, DECLINED, UNAVAILABLE
)

data class EndCallRequest(
    val reason: String? = null // COMPLETED, CANCELLED, FAILED
)

data class CallPageResponse(
    val content: List<CallDto>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)
