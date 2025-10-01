package com.summitcodeworks.networkmonitor.database

import androidx.room.*
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkSummary
import com.summitcodeworks.networkmonitor.model.NetworkType
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkLogDao {

    @Query("SELECT * FROM network_logs ORDER BY requestTime DESC")
    fun getAllLogs(): Flow<List<NetworkLog>>

    @Query("SELECT * FROM network_logs WHERE id = :id")
    suspend fun getLogById(id: Long): NetworkLog?

    @Query("SELECT * FROM network_logs WHERE type = :type ORDER BY requestTime DESC")
    fun getLogsByType(type: NetworkType): Flow<List<NetworkLog>>

    @Query("SELECT * FROM network_logs WHERE requestTime >= :startTime AND requestTime <= :endTime ORDER BY requestTime DESC")
    fun getLogsByTimeRange(startTime: Long, endTime: Long): Flow<List<NetworkLog>>

    @Query("SELECT * FROM network_logs WHERE url LIKE '%' || :searchQuery || '%' OR method LIKE '%' || :searchQuery || '%' ORDER BY requestTime DESC")
    fun searchLogs(searchQuery: String): Flow<List<NetworkLog>>

    @Insert
    suspend fun insertLog(log: NetworkLog): Long

    @Update
    suspend fun updateLog(log: NetworkLog)

    @Delete
    suspend fun deleteLog(log: NetworkLog)

    @Query("DELETE FROM network_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM network_logs WHERE requestTime < :timestamp")
    suspend fun deleteLogsOlderThan(timestamp: Long)

    @Query("""
        SELECT
            COUNT(*) as totalRequests,
            SUM(CASE WHEN responseCode >= 200 AND responseCode < 300 THEN 1 ELSE 0 END) as successfulRequests,
            SUM(CASE WHEN responseCode >= 400 OR error IS NOT NULL THEN 1 ELSE 0 END) as failedRequests,
            SUM(requestSize + responseSize) as totalDataTransferred,
            AVG(duration) as averageResponseTime
        FROM network_logs
        WHERE requestTime >= :startTime
    """)
    suspend fun getNetworkSummary(startTime: Long): NetworkSummary?
    
    @Query("""
        DELETE FROM network_logs 
        WHERE id NOT IN (
            SELECT MAX(id) 
            FROM network_logs 
            GROUP BY requestId
        )
    """)
    suspend fun removeDuplicateLogs()

    @Query("SELECT COUNT(*) FROM network_logs")
    suspend fun getLogCount(): Int

    @Query("SELECT * FROM network_logs WHERE responseCode >= 400 OR error IS NOT NULL ORDER BY requestTime DESC")
    fun getFailedRequests(): Flow<List<NetworkLog>>
}