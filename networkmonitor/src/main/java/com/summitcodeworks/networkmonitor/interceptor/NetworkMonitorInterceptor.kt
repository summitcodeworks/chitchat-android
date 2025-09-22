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
            protocol = "HTTP/1.1"
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
            // Find and update the log - simplified approach
            val updatedLog = NetworkLog(
                requestId = requestId,
                type = NetworkType.HTTP,
                method = response.request.method,
                url = response.request.url.toString(),
                requestHeaders = headersToString(response.request.headers),
                requestBody = getRequestBody(response.request),
                responseCode = response.code,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                requestTime = requestTime,
                responseTime = responseTime,
                duration = duration,
                requestSize = calculateRequestSize(response.request),
                responseSize = responseSize,
                isSSL = response.request.url.isHttps,
                protocol = response.protocol.toString()
            )

            networkLogDao.insertLog(updatedLog)
        } else if (exception != null) {
            val errorLog = NetworkLog(
                requestId = requestId,
                type = NetworkType.HTTP,
                method = "",
                url = "",
                requestTime = requestTime,
                responseTime = responseTime,
                duration = duration,
                error = exception.message
            )

            networkLogDao.insertLog(errorLog)
        }
    }

    private fun headersToString(headers: okhttp3.Headers): String {
        return gson.toJson(headers.toMultimap())
    }

    private fun getRequestBody(request: Request): String? {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            "Failed to read request body: ${e.message}"
        }
    }

    private fun getResponseBody(response: Response): String? {
        return try {
            if (isPlainText(response.headers["Content-Type"])) {
                val source = response.body?.source()
                source?.let {
                    val buffer = it.buffer
                    buffer.clone().readUtf8()
                }
            } else {
                "[Binary Content - ${response.headers["Content-Type"]}]"
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
        return contentType.startsWith("text/") ||
                contentType.contains("json") ||
                contentType.contains("xml") ||
                contentType.contains("html") ||
                contentType.contains("plain")
    }
}