package com.summitcodeworks.networkmonitor.database

import androidx.room.TypeConverter
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.model.WebSocketEventType

class Converters {

    @TypeConverter
    fun fromNetworkType(type: NetworkType): String {
        return type.name
    }

    @TypeConverter
    fun toNetworkType(type: String): NetworkType {
        return NetworkType.valueOf(type)
    }

    @TypeConverter
    fun fromWebSocketEventType(type: WebSocketEventType): String {
        return type.name
    }

    @TypeConverter
    fun toWebSocketEventType(type: String): WebSocketEventType {
        return WebSocketEventType.valueOf(type)
    }
}