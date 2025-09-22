package com.summitcodeworks.networkmonitor.database

import androidx.room.*
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import com.summitcodeworks.networkmonitor.model.WebSocketEventType
import kotlinx.coroutines.flow.Flow

@Dao
interface WebSocketEventDao {

    @Query("SELECT * FROM websocket_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<WebSocketEvent>>

    @Query("SELECT * FROM websocket_events WHERE connectionId = :connectionId ORDER BY timestamp DESC")
    fun getEventsByConnectionId(connectionId: String): Flow<List<WebSocketEvent>>

    @Query("SELECT * FROM websocket_events WHERE eventType = :eventType ORDER BY timestamp DESC")
    fun getEventsByType(eventType: WebSocketEventType): Flow<List<WebSocketEvent>>

    @Query("SELECT * FROM websocket_events WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getEventsByTimeRange(startTime: Long, endTime: Long): Flow<List<WebSocketEvent>>

    @Query("SELECT DISTINCT connectionId FROM websocket_events ORDER BY timestamp DESC")
    suspend fun getAllConnectionIds(): List<String>

    @Insert
    suspend fun insertEvent(event: WebSocketEvent): Long

    @Delete
    suspend fun deleteEvent(event: WebSocketEvent)

    @Query("DELETE FROM websocket_events")
    suspend fun clearAllEvents()

    @Query("DELETE FROM websocket_events WHERE timestamp < :timestamp")
    suspend fun deleteEventsOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM websocket_events")
    suspend fun getEventCount(): Int

    @Query("SELECT * FROM websocket_events WHERE eventType = 'FAILURE' ORDER BY timestamp DESC")
    fun getFailedEvents(): Flow<List<WebSocketEvent>>
}