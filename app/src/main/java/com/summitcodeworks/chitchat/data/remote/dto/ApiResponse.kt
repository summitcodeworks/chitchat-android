package com.summitcodeworks.chitchat.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val timestamp: String
)

data class ApiErrorResponse(
    val success: Boolean,
    val message: String,
    val errors: List<FieldError>? = null,
    val timestamp: String
)

data class FieldError(
    val field: String,
    val message: String
)
