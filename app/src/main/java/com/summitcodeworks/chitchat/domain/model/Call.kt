package com.summitcodeworks.chitchat.domain.model

data class Call(
    val sessionId: String,
    val callerId: Long,
    val calleeId: Long,
    val callType: CallType,
    val status: CallStatus,
    val startTime: String,
    val endTime: String? = null,
    val duration: Long? = null, // in seconds
    val groupId: Long? = null,
    val caller: User? = null,
    val callee: User? = null
)

enum class CallType {
    VOICE, VIDEO
}

enum class CallStatus {
    INITIATED, RINGING, ANSWERED, REJECTED, ENDED, MISSED
}
