package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.remote.api.AdminApiService
import com.summitcodeworks.chitchat.data.remote.api.AdminLoginRequest
import com.summitcodeworks.chitchat.data.remote.api.AdminAuthResponse
import com.summitcodeworks.chitchat.data.remote.api.AnalyticsResponse
import com.summitcodeworks.chitchat.data.remote.api.ManageUserRequest
import com.summitcodeworks.chitchat.data.remote.api.UserDataExportResponse
import com.summitcodeworks.chitchat.data.remote.api.UserPageResponse
import com.summitcodeworks.chitchat.data.remote.api.AdminUserDetailsResponse
import com.summitcodeworks.chitchat.data.remote.api.StatisticsResponse
import com.summitcodeworks.chitchat.data.remote.api.SystemHealthResponse
import com.summitcodeworks.chitchat.data.remote.api.LogsResponse
import com.summitcodeworks.chitchat.data.remote.dto.*
import com.summitcodeworks.chitchat.data.remote.error.NetworkErrorHandler
import com.summitcodeworks.chitchat.data.remote.error.withRetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

interface AdminRepository {
    suspend fun adminLogin(username: String, password: String): Result<AdminAuthResponse>
    suspend fun getAnalytics(token: String, startDate: String, endDate: String): Result<AnalyticsResponse>
    suspend fun manageUser(token: String, userId: Long, action: String, reason: String?, duration: String?): Result<Unit>
    suspend fun exportUserData(token: String, userId: Long): Result<UserDataExportResponse>
    suspend fun getAllUsers(token: String, page: Int = 0, size: Int = 20): Result<UserPageResponse>
    suspend fun getUserDetails(token: String, userId: Long): Result<AdminUserDetailsResponse>
    suspend fun getStatistics(token: String): Result<StatisticsResponse>
    suspend fun getSystemHealth(token: String): Result<SystemHealthResponse>
    suspend fun getSystemLogs(token: String, level: String?, service: String?, page: Int = 0, size: Int = 50): Result<LogsResponse>
}

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val adminApiService: AdminApiService,
    private val errorHandler: NetworkErrorHandler
) : AdminRepository {
    
    override suspend fun adminLogin(username: String, password: String): Result<AdminAuthResponse> {
        return withRetry(errorHandler) {
            val request = AdminLoginRequest(username = username, password = password)
            val response = adminApiService.adminLogin(request)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Admin login failed")
            }
        }
    }
    
    override suspend fun getAnalytics(token: String, startDate: String, endDate: String): Result<AnalyticsResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.getAnalytics(token, startDate, endDate)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to get analytics")
            }
        }
    }
    
    override suspend fun manageUser(token: String, userId: Long, action: String, reason: String?, duration: String?): Result<Unit> {
        return withRetry(errorHandler) {
            val request = ManageUserRequest(
                userId = userId,
                action = action,
                reason = reason,
                duration = duration
            )
            val response = adminApiService.manageUser(token, request)
            if (response.success) {
                Unit
            } else {
                throw Exception(response.message ?: "Failed to manage user")
            }
        }
    }
    
    override suspend fun exportUserData(token: String, userId: Long): Result<UserDataExportResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.exportUserData(token, userId)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to export user data")
            }
        }
    }
    
    override suspend fun getAllUsers(token: String, page: Int, size: Int): Result<UserPageResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.getAllUsers(token, page, size)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to get users")
            }
        }
    }
    
    override suspend fun getUserDetails(token: String, userId: Long): Result<AdminUserDetailsResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.getUserDetails(token, userId)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to get user details")
            }
        }
    }
    
    override suspend fun getStatistics(token: String): Result<StatisticsResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.getStatistics(token)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to get statistics")
            }
        }
    }
    
    override suspend fun getSystemHealth(token: String): Result<SystemHealthResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.getSystemHealth(token)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to get system health")
            }
        }
    }
    
    override suspend fun getSystemLogs(token: String, level: String?, service: String?, page: Int, size: Int): Result<LogsResponse> {
        return withRetry(errorHandler) {
            val response = adminApiService.getSystemLogs(token, level, service, page, size)
            if (response.success) {
                response.data!!
            } else {
                throw Exception(response.message ?: "Failed to get system logs")
            }
        }
    }
}
