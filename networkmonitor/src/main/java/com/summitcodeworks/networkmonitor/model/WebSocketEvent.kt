package com.summitcodeworks.networkmonitor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "websocket_events")
data class WebSocketEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val connectionId: String,
    val url: String,
    val eventType: WebSocketEventType,
    val message: String? = null,
    val timestamp: Long,
    val error: String? = null
)

enum class WebSocketEventType {
    OPENING,
    OPEN,
    MESSAGE_SENT,
    MESSAGE_RECEIVED,
    CLOSING,
    CLOSED,
    FAILURE
}