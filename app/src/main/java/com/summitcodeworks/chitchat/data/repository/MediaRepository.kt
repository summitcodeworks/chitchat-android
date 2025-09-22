package com.summitcodeworks.chitchat.data.repository

import com.summitcodeworks.chitchat.data.local.dao.MediaDao
import com.summitcodeworks.chitchat.data.mapper.MediaMapper
import com.summitcodeworks.chitchat.data.remote.api.GenerateThumbnailRequest
import com.summitcodeworks.chitchat.data.remote.api.MediaApiService
import com.summitcodeworks.chitchat.data.remote.api.CompressMediaRequest
import com.summitcodeworks.chitchat.domain.model.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface MediaRepository {
    suspend fun uploadMedia(
        token: String,
        file: File,
        type: String,
        description: String? = null,
        messageId: String? = null,
        statusId: Long? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<Media>
    
    suspend fun getMediaInfo(token: String, mediaId: Long): Result<Media>
    
    suspend fun downloadMedia(token: String, mediaId: Long): Result<String> // Returns download URL
    
    suspend fun deleteMedia(token: String, mediaId: Long): Result<Unit>
    
    suspend fun getUserMedia(
        token: String,
        userId: Long,
        type: String? = null,
        page: Int = 0,
        limit: Int = 20
    ): Result<List<Media>>
    
    suspend fun getMessageMedia(token: String, messageId: String): Result<List<Media>>
    
    suspend fun generateThumbnail(
        token: String,
        mediaId: Long,
        width: Int? = null,
        height: Int? = null,
        quality: Int = 80
    ): Result<Media>
    
    suspend fun searchMedia(
        token: String,
        query: String,
        type: String? = null,
        page: Int = 0,
        limit: Int = 20
    ): Result<List<Media>>
    
    suspend fun compressMedia(
        token: String,
        mediaId: Long,
        quality: Int = 80,
        maxWidth: Int? = null,
        maxHeight: Int? = null
    ): Result<Media>
    
    suspend fun getStorageQuota(token: String): Result<Map<String, Any>>
    
    fun getUserMediaFlow(userId: Long): Flow<List<Media>>
    
    fun getMediaFlow(mediaId: Long): Flow<Media?>
}

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaApiService: MediaApiService,
    private val mediaDao: MediaDao,
    private val mediaMapper: MediaMapper
) : MediaRepository {
    
    override suspend fun uploadMedia(
        token: String,
        file: File,
        type: String,
        description: String?,
        messageId: String?,
        statusId: Long?,
        onProgress: (Float) -> Unit
    ): Result<Media> {
        return try {
            // Create multipart request body
            val requestFile = file.asRequestBody(
                getContentType(type).toMediaTypeOrNull()
            )
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val messageIdPart = messageId?.toRequestBody("text/plain".toMediaTypeOrNull())
            val statusIdPart = statusId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = mediaApiService.uploadMedia(
                token = token,
                file = filePart,
                type = typePart,
                description = descriptionPart,
                messageId = messageIdPart,
                statusId = statusIdPart
            )
            
            if (response.success) {
                val media = mediaMapper.toDomain(response.data!!)
                
                // Save to local database
                mediaDao.insertMedia(mediaMapper.toEntity(media))
                
                Result.success(media)
            } else {
                Result.failure(Exception(response.message ?: "Failed to upload media"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getMediaInfo(token: String, mediaId: Long): Result<Media> {
        return try {
            val response = mediaApiService.getMediaInfo(token, mediaId)
            if (response.success) {
                val media = mediaMapper.toDomain(response.data!!)
                
                // Save to local database
                mediaDao.insertMedia(mediaMapper.toEntity(media))
                
                Result.success(media)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get media info"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedMedia = mediaDao.getMediaById(mediaId)
            if (cachedMedia != null) {
                Result.success(mediaMapper.toDomain(cachedMedia))
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun downloadMedia(token: String, mediaId: Long): Result<String> {
        return try {
            val response = mediaApiService.downloadMedia(token, mediaId)
            if (response.success) {
                val downloadUrl = response.data?.downloadUrl ?: ""
                Result.success(downloadUrl)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get download URL"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMedia(token: String, mediaId: Long): Result<Unit> {
        return try {
            val response = mediaApiService.deleteMedia(token, mediaId)
            if (response.success) {
                // Remove from local database
                mediaDao.deleteMediaById(mediaId)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to delete media"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserMedia(
        token: String,
        userId: Long,
        type: String?,
        page: Int,
        limit: Int
    ): Result<List<Media>> {
        return try {
            val response = mediaApiService.getUserMedia(token, userId, type, page, limit)
            if (response.success) {
                val mediaList = response.data?.map { mediaMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                mediaList.forEach { media ->
                    mediaDao.insertMedia(mediaMapper.toEntity(media))
                }
                
                Result.success(mediaList)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get user media"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedMedia = mediaDao.getUserMedia(userId, type).map { mediaMapper.toDomain(it) }
            if (cachedMedia.isNotEmpty()) {
                Result.success(cachedMedia)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getMessageMedia(token: String, messageId: String): Result<List<Media>> {
        return try {
            val response = mediaApiService.getMessageMedia(token, messageId)
            if (response.success) {
                val mediaList = response.data?.map { mediaMapper.toDomain(it) } ?: emptyList()
                
                // Save to local database
                mediaList.forEach { media ->
                    mediaDao.insertMedia(mediaMapper.toEntity(media))
                }
                
                Result.success(mediaList)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get message media"))
            }
        } catch (e: Exception) {
            // Return cached data if available
            val cachedMedia = mediaDao.getMessageMedia(messageId).map { mediaMapper.toDomain(it) }
            if (cachedMedia.isNotEmpty()) {
                Result.success(cachedMedia)
            } else {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun generateThumbnail(
        token: String,
        mediaId: Long,
        width: Int?,
        height: Int?,
        quality: Int
    ): Result<Media> {
        return try {
            val request = GenerateThumbnailRequest(
                width = width,
                height = height,
                quality = quality
            )
            
            val response = mediaApiService.generateThumbnail(token, mediaId, request)
            if (response.success) {
                val media = mediaMapper.toDomain(response.data!!)
                
                // Update local database
                mediaDao.insertMedia(mediaMapper.toEntity(media))
                
                Result.success(media)
            } else {
                Result.failure(Exception(response.message ?: "Failed to generate thumbnail"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchMedia(
        token: String,
        query: String,
        type: String?,
        page: Int,
        limit: Int
    ): Result<List<Media>> {
        return try {
            val response = mediaApiService.searchMedia(token, query, type, page, limit)
            if (response.success) {
                val mediaList = response.data?.map { mediaMapper.toDomain(it) } ?: emptyList()
                Result.success(mediaList)
            } else {
                Result.failure(Exception(response.message ?: "Failed to search media"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun compressMedia(
        token: String,
        mediaId: Long,
        quality: Int,
        maxWidth: Int?,
        maxHeight: Int?
    ): Result<Media> {
        return try {
            val request = CompressMediaRequest(
                mediaId = mediaId,
                quality = quality,
                maxWidth = maxWidth,
                maxHeight = maxHeight
            )
            
            val response = mediaApiService.compressMedia(token, request)
            if (response.success) {
                val media = mediaMapper.toDomain(response.data!!)
                
                // Update local database
                mediaDao.insertMedia(mediaMapper.toEntity(media))
                
                Result.success(media)
            } else {
                Result.failure(Exception(response.message ?: "Failed to compress media"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStorageQuota(token: String): Result<Map<String, Any>> {
        return try {
            val response = mediaApiService.getStorageQuota(token)
            if (response.success) {
                val quota = response.data!!
                val result = mapOf(
                    "totalQuota" to quota.totalQuota,
                    "usedQuota" to quota.usedQuota,
                    "remainingQuota" to quota.remainingQuota,
                    "quotaPercentage" to quota.quotaPercentage
                )
                Result.success(result)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get storage quota"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserMediaFlow(userId: Long): Flow<List<Media>> {
        return mediaDao.getUserMediaFlow(userId).map { entities ->
            entities.map { mediaMapper.toDomain(it) }
        }
    }
    
    override fun getMediaFlow(mediaId: Long): Flow<Media?> {
        return mediaDao.getMediaByIdFlow(mediaId).map { entity ->
            entity?.let { mediaMapper.toDomain(it) }
        }
    }
    
    private fun getContentType(type: String): String {
        return when (type.lowercase()) {
            "image" -> "image/*"
            "video" -> "video/*"
            "audio" -> "audio/*"
            "document" -> "application/*"
            else -> "application/octet-stream"
        }
    }
}
