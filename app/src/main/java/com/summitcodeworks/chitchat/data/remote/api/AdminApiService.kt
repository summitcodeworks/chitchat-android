package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.ApiResponse
import com.summitcodeworks.chitchat.data.remote.dto.UserDto
import com.summitcodeworks.chitchat.data.remote.dto.Pageable
import retrofit2.http.*

interface AdminApiService {
    
    @POST("api/admin/login")
    suspend fun adminLogin(
        @Body request: AdminLoginRequest
    ): ApiResponse<AdminAuthResponse>
    
    @GET("api/admin/analytics")
    suspend fun getAnalytics(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): ApiResponse<AnalyticsResponse>
    
    @POST("api/admin/users/manage")
    suspend fun manageUser(
        @Header("Authorization") token: String,
        @Body request: ManageUserRequest
    ): ApiResponse<Unit>
    
    @POST("api/admin/users/{userId}/export")
    suspend fun exportUserData(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): ApiResponse<UserDataExportResponse>
    
    @GET("api/admin/users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "createdAt,desc"
    ): ApiResponse<UserPageResponse>
    
    @GET("api/admin/users/{userId}")
    suspend fun getUserDetails(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): ApiResponse<AdminUserDetailsResponse>
    
    @GET("api/admin/statistics")
    suspend fun getStatistics(
        @Header("Authorization") token: String
    ): ApiResponse<StatisticsResponse>
    
    @GET("api/admin/system/health")
    suspend fun getSystemHealth(
        @Header("Authorization") token: String
    ): ApiResponse<SystemHealthResponse>
    
    @GET("api/admin/logs")
    suspend fun getSystemLogs(
        @Header("Authorization") token: String,
        @Query("level") level: String? = null,
        @Query("service") service: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ApiResponse<LogsResponse>
}

// Admin DTOs
data class AdminLoginRequest(
    val username: String,
    val password: String
)

data class AdminAuthResponse(
    val token: String,
    val admin: AdminDto
)

data class AdminDto(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val permissions: List<String>
)

data class AnalyticsResponse(
    val totalUsers: Long,
    val activeUsers: Long,
    val totalMessages: Long,
    val totalCalls: Long,
    val totalGroups: Long,
    val userGrowth: List<UserGrowthData>,
    val messageStats: MessageStats,
    val callStats: CallStats
)

data class UserGrowthData(
    val date: String,
    val newUsers: Long,
    val activeUsers: Long
)

data class MessageStats(
    val totalMessages: Long,
    val messagesToday: Long,
    val averageMessagesPerUser: Double
)

data class CallStats(
    val totalCalls: Long,
    val callsToday: Long,
    val averageCallDuration: Double
)

data class ManageUserRequest(
    val userId: Long,
    val action: String, // SUSPEND, UNSUSPEND, DELETE
    val reason: String? = null,
    val duration: String? = null // 1_DAY, 7_DAYS, 30_DAYS, PERMANENT
)

data class UserDataExportResponse(
    val exportId: String,
    val downloadUrl: String,
    val expiresAt: String
)

data class UserPageResponse(
    val content: List<UserDto>,
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

data class AdminUserDetailsResponse(
    val user: UserDto,
    val totalMessages: Long,
    val totalCalls: Long,
    val groupsJoined: Long,
    val lastActive: String,
    val accountStatus: String,
    val violations: List<ViolationDto>
)

data class ViolationDto(
    val id: Long,
    val type: String,
    val description: String,
    val reportedAt: String,
    val status: String
)

data class StatisticsResponse(
    val systemStats: SystemStats,
    val userStats: UserStats,
    val performanceStats: PerformanceStats
)

data class SystemStats(
    val uptime: String,
    val memoryUsage: Double,
    val cpuUsage: Double,
    val diskUsage: Double
)

data class UserStats(
    val totalUsers: Long,
    val activeUsers: Long,
    val newUsersToday: Long,
    val blockedUsers: Long
)

data class PerformanceStats(
    val averageResponseTime: Double,
    val requestsPerSecond: Double,
    val errorRate: Double
)

data class SystemHealthResponse(
    val status: String, // HEALTHY, DEGRADED, DOWN
    val services: List<ServiceHealth>,
    val lastChecked: String
)

data class ServiceHealth(
    val name: String,
    val status: String,
    val responseTime: Double,
    val lastChecked: String
)

data class LogsResponse(
    val content: List<LogEntry>,
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

data class LogEntry(
    val id: String,
    val timestamp: String,
    val level: String,
    val service: String,
    val message: String,
    val details: String? = null
)

