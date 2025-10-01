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

        // Insert initial log and get the generated ID synchronously
        val logId = kotlinx.coroutines.runBlocking {
            networkLogDao.insertLog(requestLog)
        }

        var response: Response? = null
        var exception: IOException? = null
        var responseBodyString: String? = null

        try {
            response = chain.proceed(request)
            
            // Read and cache the response body BEFORE it's consumed
            val responseBody = response.body
            if (responseBody != null) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE) // Buffer the entire body
                val buffer = source.buffer
                
                // Read the response body
                responseBodyString = buffer.clone().readUtf8()
                
                // Create a new response with a fresh body that can be consumed by the caller
                val contentType = responseBody.contentType()
                val newResponseBody = okhttp3.ResponseBody.create(contentType, responseBodyString)
                
                val newResponse = response.newBuilder()
                    .body(newResponseBody)
                    .build()
                
                // Update log with response data immediately (with cached body)
                scope.launch {
                    updateLogWithResponseBody(requestId, newResponse, responseBodyString, requestTime, logId)
                }
                
                return newResponse
            } else {
                // No response body, log as is
                scope.launch {
                    updateLogWithResponseBody(requestId, response, null, requestTime, logId)
                }
                return response
            }
        } catch (e: IOException) {
            exception = e
            // Update log with exception
            scope.launch {
                updateLogWithException(requestId, exception, requestTime, logId)
            }
            throw e
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

    private suspend fun updateLogWithResponseBody(
        requestId: String,
        response: Response,
        responseBodyString: String?,
        requestTime: Long,
        logId: Long
    ) {
        val responseTime = System.currentTimeMillis()
        val duration = responseTime - requestTime
        
        val responseHeaders = headersToString(response.headers)
        val responseSize = calculateResponseSize(response)
        val requestBody = getRequestBody(response.request)
        val curlCommand = generateCurlCommand(response.request, requestBody)

        val updatedLog = NetworkLog(
            id = logId, // Use the same ID to update the existing log
            requestId = requestId,
            type = NetworkType.HTTP,
            method = response.request.method,
            url = response.request.url.toString(),
            requestHeaders = headersToString(response.request.headers),
            requestBody = requestBody,
            responseCode = response.code,
            responseHeaders = responseHeaders,
            responseBody = responseBodyString,
            requestTime = requestTime,
            responseTime = responseTime,
            duration = duration,
            requestSize = calculateRequestSize(response.request),
            responseSize = responseSize,
            isSSL = response.request.url.isHttps,
            protocol = response.protocol.toString(),
            curlCommand = curlCommand
        )

        networkLogDao.updateLog(updatedLog) // Use UPDATE instead of INSERT
    }
    
    private suspend fun updateLogWithException(
        requestId: String,
        exception: IOException,
        requestTime: Long,
        logId: Long
    ) {
        val responseTime = System.currentTimeMillis()
        val duration = responseTime - requestTime
        
        // Get the existing log to preserve request data
        val existingLog = networkLogDao.getLogById(logId)
        
        val errorLog = NetworkLog(
            id = logId, // Use the same ID to update
            requestId = requestId,
            type = NetworkType.HTTP,
            method = existingLog?.method ?: "UNKNOWN",
            url = existingLog?.url ?: "UNKNOWN",
            requestHeaders = existingLog?.requestHeaders,
            requestBody = existingLog?.requestBody,
            requestTime = requestTime,
            responseTime = responseTime,
            duration = duration,
            requestSize = existingLog?.requestSize ?: 0,
            isSSL = existingLog?.isSSL ?: false,
            protocol = existingLog?.protocol ?: "HTTP/1.1",
            curlCommand = existingLog?.curlCommand,
            error = "Exception: ${exception.javaClass.simpleName} - ${exception.message}\nStack trace: ${exception.stackTraceToString()}"
        )

        networkLogDao.updateLog(errorLog) // Use UPDATE instead of INSERT
    }

    private fun headersToString(headers: okhttp3.Headers): String {
        val prettyGson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        return prettyGson.toJson(headers.toMultimap())
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
            // Properly escape header values
            val escapedValue = value.replace("\"", "\\\"")
            curlBuilder.append(" \\\n  -H \"$name: $escapedValue\"")
        }

        // Add body for POST/PUT/PATCH requests
        if (!requestBody.isNullOrEmpty() && request.method in listOf("POST", "PUT", "PATCH")) {
            val contentType = request.header("Content-Type")?.lowercase()

            when {
                contentType?.contains("application/json") == true -> {
                    // For JSON, minify it first to make cURL more readable
                    val minifiedJson = try {
                        val jsonElement = com.google.gson.JsonParser.parseString(requestBody)
                        gson.toJson(jsonElement)  // This creates compact JSON
                    } catch (e: Exception) {
                        requestBody
                    }
                    
                    // Escape for shell
                    val escapedBody = minifiedJson.replace("'", "'\\''")  // Escape single quotes for single-quote wrapping
                    curlBuilder.append(" \\\n  -d '").append(escapedBody).append("'")
                }
                contentType?.contains("application/x-www-form-urlencoded") == true -> {
                    // For form data, use single quotes
                    val escapedBody = requestBody.replace("'", "'\\''")
                    curlBuilder.append(" \\\n  -d '").append(escapedBody).append("'")
                }
                else -> {
                    // For other content types, use single quotes with proper escaping
                    val escapedBody = requestBody.replace("'", "'\\''")
                    curlBuilder.append(" \\\n  -d '").append(escapedBody).append("'")
                }
            }
        }

        // Add URL (always last)
        curlBuilder.append(" \\\n  \"${request.url}\"")

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