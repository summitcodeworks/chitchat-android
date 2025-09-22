package com.summitcodeworks.chitchat.data.remote.dto

data class CompressMediaRequest(
    val mediaUrl: String,
    val mediaType: String, // "IMAGE", "VIDEO", "AUDIO"
    val quality: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH"
    val maxFileSize: Long? = null // max size in bytes
)
