package com.summitcodeworks.chitchat.data.remote.api

import com.summitcodeworks.chitchat.data.remote.dto.ApiResponse
import com.summitcodeworks.chitchat.data.remote.dto.MediaDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MediaApiService {
    
    @Multipart
    @POST("media/upload")
    suspend fun uploadMedia(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody,
        @Part("description") description: RequestBody? = null,
        @Part("messageId") messageId: RequestBody? = null,
        @Part("statusId") statusId: RequestBody? = null
    ): ApiResponse<MediaDto>
    
    @GET("media/{mediaId}")
    suspend fun getMediaInfo(
        @Header("Authorization") token: String,
        @Path("mediaId") mediaId: Long
    ): ApiResponse<MediaDto>
    
    @GET("media/{mediaId}/download")
    suspend fun downloadMedia(
        @Header("Authorization") token: String,
        @Path("mediaId") mediaId: Long
    ): ApiResponse<MediaDownloadResponse>
    
    @DELETE("media/{mediaId}")
    suspend fun deleteMedia(
        @Header("Authorization") token: String,
        @Path("mediaId") mediaId: Long
    ): ApiResponse<Unit>
    
    @GET("media/user/{userId}")
    suspend fun getUserMedia(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<MediaDto>>
    
    @GET("media/message/{messageId}")
    suspend fun getMessageMedia(
        @Header("Authorization") token: String,
        @Path("messageId") messageId: String
    ): ApiResponse<List<MediaDto>>
    
    @POST("media/{mediaId}/thumbnail")
    suspend fun generateThumbnail(
        @Header("Authorization") token: String,
        @Path("mediaId") mediaId: Long,
        @Body request: GenerateThumbnailRequest
    ): ApiResponse<MediaDto>
    
    @GET("media/search")
    suspend fun searchMedia(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<MediaDto>>
    
    @POST("media/compress")
    suspend fun compressMedia(
        @Header("Authorization") token: String,
        @Body request: CompressMediaRequest
    ): ApiResponse<MediaDto>
    
    @GET("media/storage/quota")
    suspend fun getStorageQuota(
        @Header("Authorization") token: String
    ): ApiResponse<StorageQuotaResponse>
}

data class MediaDownloadResponse(
    val downloadUrl: String,
    val expiresAt: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)

data class GenerateThumbnailRequest(
    val width: Int? = null,
    val height: Int? = null,
    val quality: Int = 80
)

data class CompressMediaRequest(
    val mediaId: Long,
    val quality: Int = 80,
    val maxWidth: Int? = null,
    val maxHeight: Int? = null
)

data class StorageQuotaResponse(
    val totalQuota: Long,
    val usedQuota: Long,
    val remainingQuota: Long,
    val quotaPercentage: Float
)