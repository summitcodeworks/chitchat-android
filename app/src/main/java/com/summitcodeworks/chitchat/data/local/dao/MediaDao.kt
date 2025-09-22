package com.summitcodeworks.chitchat.data.local.dao

import androidx.room.*
import com.summitcodeworks.chitchat.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    
    @Query("SELECT * FROM media WHERE id = :mediaId")
    suspend fun getMediaById(mediaId: Long): MediaEntity?
    
    @Query("SELECT * FROM media WHERE uploadedBy = :userId AND (:type IS NULL OR mediaType = :type) ORDER BY uploadedAt DESC")
    suspend fun getUserMedia(userId: Long, type: String?): List<MediaEntity>
    
    @Query("SELECT * FROM media WHERE uploadedBy = :userId ORDER BY uploadedAt DESC")
    fun getUserMediaFlow(userId: Long): Flow<List<MediaEntity>>
    
    @Query("SELECT * FROM media WHERE id = :mediaId")
    fun getMediaByIdFlow(mediaId: Long): Flow<MediaEntity?>
    
    @Query("SELECT * FROM media WHERE messageId = :messageId ORDER BY uploadedAt DESC")
    suspend fun getMessageMedia(messageId: String): List<MediaEntity>
    
    @Query("SELECT * FROM media WHERE mediaType = :mediaType ORDER BY uploadedAt DESC")
    fun getMediaByType(mediaType: String): Flow<List<MediaEntity>>
    
    @Query("SELECT * FROM media WHERE uploadedBy = :userId AND mediaType = :mediaType ORDER BY uploadedAt DESC")
    fun getUserMediaByType(userId: Long, mediaType: String): Flow<List<MediaEntity>>
    
    @Query("SELECT * FROM media ORDER BY uploadedAt DESC LIMIT :limit")
    suspend fun getRecentMedia(limit: Int = 10): List<MediaEntity>
    
    @Query("SELECT * FROM media WHERE uploadedBy = :userId ORDER BY uploadedAt DESC LIMIT :limit")
    suspend fun getUserRecentMedia(userId: Long, limit: Int = 10): List<MediaEntity>
    
    @Query("SELECT * FROM media WHERE fileName LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY uploadedAt DESC")
    suspend fun searchMedia(query: String): List<MediaEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)
    
    @Update
    suspend fun updateMedia(media: MediaEntity)
    
    @Query("UPDATE media SET description = :description WHERE id = :mediaId")
    suspend fun updateMediaDescription(mediaId: Long, description: String)
    
    @Delete
    suspend fun deleteMedia(media: MediaEntity)
    
    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: Long)
    
    @Query("DELETE FROM media WHERE uploadedBy = :userId")
    suspend fun deleteUserMedia(userId: Long)
    
    @Query("DELETE FROM media")
    suspend fun deleteAllMedia()
}
