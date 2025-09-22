package com.summitcodeworks.chitchat.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for all API responses from the ChitChat backend
 */
data class ApiResponseWrapper<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("error")
    val error: ApiError? = null,
    
    @SerializedName("timestamp")
    val timestamp: String? = null,
    
    @SerializedName("requestId")
    val requestId: String? = null
)

/**
 * Error details from API responses
 */
data class ApiError(
    @SerializedName("code")
    val code: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("details")
    val details: Map<String, Any>? = null,
    
    @SerializedName("field")
    val field: String? = null
)

/**
 * Paginated response wrapper
 */
data class PaginatedResponse<T>(
    @SerializedName("items")
    val items: List<T>,
    
    @SerializedName("totalItems")
    val totalItems: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("currentPage")
    val currentPage: Int,
    
    @SerializedName("itemsPerPage")
    val itemsPerPage: Int,
    
    @SerializedName("hasNext")
    val hasNext: Boolean,
    
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean
)

/**
 * Common API response codes
 */
object ApiResponseCodes {
    const val SUCCESS = "SUCCESS"
    const val ERROR = "ERROR"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FORBIDDEN = "FORBIDDEN"
    const val NOT_FOUND = "NOT_FOUND"
    const val CONFLICT = "CONFLICT"
    const val RATE_LIMITED = "RATE_LIMITED"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
}

/**
 * Extension functions for easier API response handling
 */
fun <T> ApiResponseWrapper<T>.isSuccess(): Boolean = success && data != null

fun <T> ApiResponseWrapper<T>.isError(): Boolean = !success || error != null

fun <T> ApiResponseWrapper<T>.getErrorMessage(): String {
    return error?.message ?: message ?: "Unknown error occurred"
}

fun <T> ApiResponseWrapper<T>.getErrorCode(): String {
    return error?.code ?: ApiResponseCodes.ERROR
}
