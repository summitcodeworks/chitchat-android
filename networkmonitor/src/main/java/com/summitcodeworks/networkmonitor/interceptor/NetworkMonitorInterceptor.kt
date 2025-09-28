package com.summitcodeworks.networkmonitor.interceptor

import com.google.gson.Gson
import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitorInterceptor @Inject constructor(
    private val networkLogDao: NetworkLogDao,
    private val gson: Gson
) : Interceptor {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestId = UUID.randomUUID().toString()
        val requestTime = System.currentTimeMillis()

        // Create initial log entry
        val requestLog = createRequestLog(request, requestId, requestTime)

        // Insert initial log
        scope.launch {
            networkLogDao.insertLog(requestLog)
        }

        var response: Response? = null
        var exception: IOException? = null

        try {
            response = chain.proceed(request)
            return response
        } catch (e: IOException) {
            exception = e
            throw e
        } finally {
            // Update log with response data
            scope.launch {
                updateLogWithResponse(requestId, response, exception, requestTime)
            }
        }
    }

    private fun createRequestLog(request: Request, requestId: String, requestTime: Long): NetworkLog {
        val requestHeaders = headersToString(request.headers)
        val requestBody = getRequestBody(request)
        val requestSize = calculateRequestSize(request)
        val curlCommand = generateCurlCommand(request, requestBody)

        return NetworkLog(
            requestId = requestId,
            type = NetworkType.HTTP,
            method = request.method,
            url = request.url.toString(),
            requestHeaders = requestHeaders,
            requestBody = requestBody,
            requestTime = requestTime,
            requestSize = requestSize,
            isSSL = request.url.isHttps,
            protocol = "HTTP/1.1",
            curlCommand = curlCommand
        )
    }

    private suspend fun updateLogWithResponse(
        requestId: String,
        response: Response?,
        exception: IOException?,
        requestTime: Long
    ) {
        val responseTime = System.currentTimeMillis()
        val duration = responseTime - requestTime

        if (response != null) {
            val responseHeaders = headersToString(response.headers)
            val responseBody = getResponseBody(response)
            val responseSize = calculateResponseSize(response)

            // Get the existing log and update it
            val logs = networkLogDao.getAllLogs()
            val requestBody = getRequestBody(response.request)
            val curlCommand = generateCurlCommand(response.request, requestBody)

            // Find and update the log - simplified approach
            val updatedLog = NetworkLog(
                requestId = requestId,
                type = NetworkType.HTTP,
                method = response.request.method,
                url = response.request.url.toString(),
                requestHeaders = headersToString(response.request.headers),
                requestBody = requestBody,
                responseCode = response.code,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                requestTime = requestTime,
                responseTime = responseTime,
                duration = duration,
                requestSize = calculateRequestSize(response.request),
                responseSize = responseSize,
                isSSL = response.request.url.isHttps,
                protocol = response.protocol.toString(),
                curlCommand = curlCommand
            )

            networkLogDao.insertLog(updatedLog)
        } else if (exception != null) {
            // Create error log without looking up original log
            val errorLog = NetworkLog(
                requestId = requestId,
                type = NetworkType.HTTP,
                method = "UNKNOWN",
                url = "UNKNOWN",
                requestHeaders = null,
                requestBody = null,
                requestTime = requestTime,
                responseTime = responseTime,
                duration = duration,
                requestSize = 0,
                isSSL = false,
                protocol = "HTTP/1.1",
                curlCommand = null,
                error = "Exception: ${exception.javaClass.simpleName} - ${exception.message}\nStack trace: ${exception.stackTraceToString()}"
            )

            networkLogDao.insertLog(errorLog)
        }
    }

    private fun headersToString(headers: okhttp3.Headers): String {
        return gson.toJson(headers.toMultimap())
    }

    private fun getRequestBody(request: Request): String? {
        return try {
            val body = request.body
            if (body == null) {
                return null
            }
            
            val contentType = body.contentType()?.toString()
            if (isPlainText(contentType)) {
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } else {
                val contentLength = body.contentLength()
                "[Binary Request Body - ${contentType ?: "unknown"} (${contentLength} bytes)]"
            }
        } catch (e: Exception) {
            "Failed to read request body: ${e.message}"
        }
    }

    private fun getResponseBody(response: Response): String? {
        return try {
            val contentType = response.headers["Content-Type"]
            if (isPlainText(contentType)) {
                val source = response.body?.source()
                source?.let {
                    val buffer = it.buffer
                    // Clone the buffer to avoid consuming the original
                    val clonedBuffer = buffer.clone()
                    clonedBuffer.readUtf8()
                }
            } else if (contentType?.contains("json") == true) {
                // Handle JSON content even if not explicitly text/*
                val source = response.body?.source()
                source?.let {
                    val buffer = it.buffer
                    val clonedBuffer = buffer.clone()
                    clonedBuffer.readUtf8()
                }
            } else {
                val contentLength = response.body?.contentLength() ?: 0
                "[Binary Content - ${contentType ?: "unknown"} (${contentLength} bytes)]"
            }
        } catch (e: Exception) {
            "Failed to read response body: ${e.message}"
        }
    }

    private fun calculateRequestSize(request: Request): Long {
        return try {
            val buffer = Buffer()
            request.body?.writeTo(buffer)
            buffer.size + request.headers.byteCount()
        } catch (e: Exception) {
            0L
        }
    }

    private fun calculateResponseSize(response: Response): Long {
        return try {
            (response.body?.contentLength() ?: 0) + response.headers.byteCount()
        } catch (e: Exception) {
            0L
        }
    }

    private fun isPlainText(contentType: String?): Boolean {
        if (contentType == null) return false
        val lowerContentType = contentType.lowercase()
        return lowerContentType.startsWith("text/") ||
                lowerContentType.contains("json") ||
                lowerContentType.contains("xml") ||
                lowerContentType.contains("html") ||
                lowerContentType.contains("plain") ||
                lowerContentType.contains("javascript") ||
                lowerContentType.contains("css") ||
                lowerContentType.contains("csv") ||
                lowerContentType.contains("form-urlencoded") ||
                lowerContentType.contains("multipart") ||
                lowerContentType.contains("application/x-www-form-urlencoded")
    }

    private fun generateCurlCommand(request: Request, requestBody: String?): String {
        val curlBuilder = StringBuilder("curl")

        // Add method
        if (request.method != "GET") {
            curlBuilder.append(" -X ${request.method}")
        }

        // Add headers
        for (i in 0 until request.headers.size) {
            val name = request.headers.name(i)
            val value = request.headers.value(i)
            curlBuilder.append(" \\\n  -H '$name: $value'")
        }

        // Add body for POST/PUT/PATCH requests
        if (!requestBody.isNullOrEmpty() && request.method in listOf("POST", "PUT", "PATCH")) {
            val escapedBody = requestBody.replace("'", "'\"'\"'")
            curlBuilder.append(" \\\n  -d '$escapedBody'")
        }

        // Add URL (always last)
        curlBuilder.append(" \\\n  '${request.url}'")

        return curlBuilder.toString()
    }

    private fun generateWebSocketInfo(url: String, headers: okhttp3.Headers): String {
        val wsBuilder = StringBuilder("WebSocket Connection:")
        wsBuilder.append("\nURL: $url")
        wsBuilder.append("\nProtocol: WebSocket")

        if (headers.size > 0) {
            wsBuilder.append("\nHeaders:")
            for (i in 0 until headers.size) {
                wsBuilder.append("\n  ${headers.name(i)}: ${headers.value(i)}")
            }
        }

        return wsBuilder.toString()
    }
}