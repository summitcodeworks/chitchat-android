package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Media
import com.summitcodeworks.chitchat.domain.usecase.media.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val getMediaInfoUseCase: GetMediaInfoUseCase,
    private val downloadMediaUseCase: DownloadMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val getUserMediaUseCase: GetUserMediaUseCase
) : ViewModel() {
    
    private val _userMedia = MutableStateFlow<List<Media>>(emptyList())
    val userMedia: StateFlow<List<Media>> = _userMedia.asStateFlow()
    
    private val _selectedMedia = MutableStateFlow<Media?>(null)
    val selectedMedia: StateFlow<Media?> = _selectedMedia.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun uploadMedia(
        token: String,
        file: File,
        type: String,
        description: String? = null,
        messageId: String? = null,
        statusId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _uploadProgress.value = 0f
            
            uploadMediaUseCase(token, file, type, description, messageId, statusId) { progress ->
                _uploadProgress.value = progress
            }
                .fold(
                    onSuccess = { media ->
                        _userMedia.value = _userMedia.value + media
                        _isLoading.value = false
                        _uploadProgress.value = 1f
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                        _uploadProgress.value = 0f
                    }
                )
        }
    }
    
    fun getMediaInfo(token: String, mediaId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getMediaInfoUseCase(token, mediaId)
                .fold(
                    onSuccess = { media ->
                        _selectedMedia.value = media
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun downloadMedia(token: String, mediaId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            downloadMediaUseCase(token, mediaId)
                .fold(
                    onSuccess = { downloadUrl ->
                        // Handle download URL (e.g., open in browser or download file)
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun deleteMedia(token: String, mediaId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            deleteMediaUseCase(token, mediaId)
                .fold(
                    onSuccess = {
                        _userMedia.value = _userMedia.value.filter { it.id != mediaId }
                        if (_selectedMedia.value?.id == mediaId) {
                            _selectedMedia.value = null
                        }
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun loadUserMedia(
        token: String,
        userId: Long,
        type: String? = null,
        page: Int = 0,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            getUserMediaUseCase(token, userId, type, page, limit)
                .fold(
                    onSuccess = { mediaList ->
                        _userMedia.value = mediaList
                        _isLoading.value = false
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                        _isLoading.value = false
                    }
                )
        }
    }
    
    fun observeUserMedia(userId: Long) {
        viewModelScope.launch {
            getUserMediaUseCase.getUserMediaFlow(userId)
                .collect { mediaList ->
                    _userMedia.value = mediaList
                }
        }
    }
    
    fun selectMedia(media: Media) {
        _selectedMedia.value = media
    }
    
    fun clearSelectedMedia() {
        _selectedMedia.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearUploadProgress() {
        _uploadProgress.value = 0f
    }
}
