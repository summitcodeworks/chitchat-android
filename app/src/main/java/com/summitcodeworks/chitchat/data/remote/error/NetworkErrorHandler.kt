package com.summitcodeworks.chitchat.data.remote.error

import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkErrorHandler @Inject constructor() {
    
    fun handleError(throwable: Throwable): NetworkError {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    400 -> NetworkError.BadRequest(throwable.message())
                    401 -> NetworkError.Unauthorized(throwable.message())
                    403 -> NetworkError.Forbidden(throwable.message())
                    404 -> NetworkError.NotFound(throwable.message())
                    409 -> NetworkError.Conflict(throwable.message())
                    422 -> NetworkError.ValidationError(throwable.message())
                    429 -> NetworkError.TooManyRequests(throwable.message())
                    500 -> NetworkError.InternalServerError(throwable.message())
                    502 -> NetworkError.BadGateway(throwable.message())
                    503 -> NetworkError.ServiceUnavailable(throwable.message())
                    504 -> NetworkError.GatewayTimeout(throwable.message())
                    else -> NetworkError.HttpError(throwable.code(), throwable.message())
                }
            }
            is SocketTimeoutException -> NetworkError.Timeout(throwable.message)
            is UnknownHostException -> NetworkError.NetworkUnavailable(throwable.message)
            is IOException -> NetworkError.GenericNetworkError(throwable.message)
            else -> NetworkError.UnknownError(throwable.message)
        }
    }
    
    fun shouldRetry(error: NetworkError): Boolean {
        return when (error) {
            is NetworkError.Timeout,
            is NetworkError.NetworkUnavailable,
            is NetworkError.GenericNetworkError,
            is NetworkError.InternalServerError,
            is NetworkError.BadGateway,
            is NetworkError.ServiceUnavailable,
            is NetworkError.GatewayTimeout -> true
            else -> false
        }
    }
    
    fun getRetryDelay(attempt: Int): Long {
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s, max 30s
        return minOf(1000L * (1 shl attempt), 30000L)
    }
}

sealed class NetworkError(val message: String?) {
    class BadRequest(message: String?) : NetworkError(message)
    class Unauthorized(message: String?) : NetworkError(message)
    class Forbidden(message: String?) : NetworkError(message)
    class NotFound(message: String?) : NetworkError(message)
    class Conflict(message: String?) : NetworkError(message)
    class ValidationError(message: String?) : NetworkError(message)
    class TooManyRequests(message: String?) : NetworkError(message)
    class HttpError(val code: Int, message: String?) : NetworkError(message)
    class Timeout(message: String?) : NetworkError(message)
    class NetworkUnavailable(message: String?) : NetworkError(message)
    class GenericNetworkError(message: String?) : NetworkError(message)
    class InternalServerError(message: String?) : NetworkError(message)
    class BadGateway(message: String?) : NetworkError(message)
    class ServiceUnavailable(message: String?) : NetworkError(message)
    class GatewayTimeout(message: String?) : NetworkError(message)
    class UnknownError(message: String?) : NetworkError(message)
}

suspend inline fun <T> withRetry(
    errorHandler: NetworkErrorHandler,
    maxRetries: Int = 3,
    crossinline operation: suspend () -> T
): Result<T> {
    var lastError: Throwable? = null
    
    repeat(maxRetries + 1) { attempt ->
        try {
            val result = operation()
            return Result.success(result)
        } catch (e: Throwable) {
            lastError = e
            val error = errorHandler.handleError(e)
            
            if (!errorHandler.shouldRetry(error) || attempt == maxRetries) {
                return Result.failure(e)
            }
            
            val delay = errorHandler.getRetryDelay(attempt)
            delay(delay)
        }
    }
    
    return Result.failure(lastError ?: Exception("Unknown error"))
}
