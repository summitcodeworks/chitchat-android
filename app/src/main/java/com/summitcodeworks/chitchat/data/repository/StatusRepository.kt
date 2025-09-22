package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.local.dao.StatusDao
import com.summitcodeworks.chitchat.data.local.entity.StatusEntity
import com.summitcodeworks.chitchat.data.mapper.StatusMapper
import com.summitcodeworks.chitchat.data.remote.api.StatusApiService
import com.summitcodeworks.chitchat.data.remote.dto.CreateStatusRequest
import com.summitcodeworks.chitchat.data.remote.dto.ReactToStatusRequest
import com.summitcodeworks.chitchat.domain.model.Status
import com.summitcodeworks.chitchat.domain.model.StatusView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface StatusRepository {
    suspend fun createStatus(
        token: String,
        content: String,
        mediaId: Long? = null,
        statusType: String = "TEXT",
        backgroundColor: String? = null,
        font: String? = null,
        privacy: String = "CONTACTS"
    ): Result<Status>
    
    suspend fun getUserStatuses(token: String, userId: Long): Result<List<Status>>
    
    suspend fun getActiveStatuses(token: String): Result<List<Status>>
    
    suspend fun getContactsStatuses(token: String, contactIds: List<Long>): Result<List<Status>>
    
    suspend fun viewStatus(token: String, statusId: Long): Result<Unit>
    
    suspend fun reactToStatus(token: String, statusId: Long, reaction: String): Result<Unit>
    
    suspend fun deleteStatus(token: String, statusId: Long): Result<Unit>
    
    suspend fun getStatusViews(token: String, statusId: Long): Result<List<StatusView>>
    
    fun getUserStatusesFlow(userId: Long): Flow<List<Status>>
    
    fun getActiveStatusesFlow(): Flow<List<Status>>
}

@Singleton
class StatusRepositoryImpl @Inject constructor(
    private val statusApiService: StatusApiService,
    private val statusDao: StatusDao,
    private val statusMapper: StatusMapper
) : StatusRepository {
    
    override suspend fun createStatus(
        token: String,
        content: String,
        mediaId: Long?,
        statusType: String,
        backgroundColor: String?,
        font: String?,
        privacy: String
    ): Result<Status> {
        return try {
            val request = CreateStatusRequest(
                content = content,
                mediaId = mediaId,
                statusType = statusType,
                backgroundColor = backgroundColor,
                font = font,
                privacy = privacy
            )
            
            val response = statusApiService.createStatus(token, request)
            if (response.isSuccessful && response.body()?.success == true) {
                val status = statusMapper.toDomain(response.body()!!.data!!)
                
                // Save to local database
                statusDao.insertStatus(statusMapper.toEntity(status))
                
                Result.success(status)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserStatuses(token: String, userId: Long): Result<List<Status>> {
        return try {
            val response = statusApiService.getUserStatuses(token, userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val statuses = response.body()!!.data?.map { statusMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                statuses.forEach { status ->
                    statusDao.insertStatus(statusMapper.toEntity(status))
                }
                
                Result.success(statuses)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get user statuses"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedStatuses = statusDao.getUserStatuses(userId).map { statusMapper.toDomain(it) }
            if (cachedStatuses.isNotEmpty()) {
                Result.success(cachedStatuses)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getActiveStatuses(token: String): Result<List<Status>> {
        return try {
            val response = statusApiService.getActiveStatuses(token)
            if (response.isSuccessful && response.body()?.success == true) {
                val statuses = response.body()!!.data?.map { statusMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                statuses.forEach { status ->
                    statusDao.insertStatus(statusMapper.toEntity(status))
                }
                
                Result.success(statuses)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get active statuses"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedStatuses = statusDao.getActiveStatuses().map { statusMapper.toDomain(it) }
            if (cachedStatuses.isNotEmpty()) {
                Result.success(cachedStatuses)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getContactsStatuses(token: String, contactIds: List<Long>): Result<List<Status>> {
        return try {
            val contactIdsString = contactIds.joinToString(",")
            val response = statusApiService.getContactsStatuses(token, contactIdsString)
            if (response.isSuccessful && response.body()?.success == true) {
                val statuses = response.body()!!.data?.map { statusMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                statuses.forEach { status ->
                    statusDao.insertStatus(statusMapper.toEntity(status))
                }
                
                Result.success(statuses)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get contacts statuses"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun viewStatus(token: String, statusId: Long): Result<Unit> {
        return try {
            val response = statusApiService.viewStatus(token, statusId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to view status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reactToStatus(token: String, statusId: Long, reaction: String): Result<Unit> {
        return try {
            val request = ReactToStatusRequest(reaction = reaction)
            val response = statusApiService.reactToStatus(token, statusId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to react to status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteStatus(token: String, statusId: Long): Result<Unit> {
        return try {
            val response = statusApiService.deleteStatus(token, statusId)
            if (response.isSuccessful && response.body()?.success == true) {
                // Remove from local database
                statusDao.deleteStatusById(statusId)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to delete status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStatusViews(token: String, statusId: Long): Result<List<StatusView>> {
        return try {
            val response = statusApiService.getStatusViews(token, statusId)
            if (response.isSuccessful && response.body()?.success == true) {
                val views = response.body()!!.data?.map { statusViewDto ->
                    StatusView(
                        id = statusViewDto.id,
                        statusId = statusViewDto.statusId,
                        userId = statusViewDto.userId,
                        viewedAt = statusViewDto.viewedAt,
                        user = statusViewDto.user?.let { userDto ->
                            com.summitcodeworks.chitchat.domain.model.User(
                                id = userDto.id,
                                phoneNumber = userDto.phoneNumber,
                                name = userDto.name,
                                avatarUrl = userDto.avatarUrl,
                                about = userDto.about,
                                isOnline = userDto.isOnline,
                                lastSeen = userDto.lastSeen
                            )
                        }
                    )
                } ?: emptyList()
                
                Result.success(views)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get status views"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserStatusesFlow(userId: Long): Flow<List<Status>> {
        return statusDao.getUserStatusesFlow(userId).map { entities ->
            entities.map { statusMapper.toDomain(it) }
        }
    }
    
    override fun getActiveStatusesFlow(): Flow<List<Status>> {
        return statusDao.getActiveStatusesFlow().map { entities ->
            entities.map { statusMapper.toDomain(it) }
        }
    }
}
