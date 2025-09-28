package com.summitcodeworks.networkmonitor.websocket

import com.summitcodeworks.networkmonitor.database.NetworkLogDao
import com.summitcodeworks.networkmonitor.database.WebSocketEventDao
import com.summitcodeworks.networkmonitor.model.NetworkLog
import com.summitcodeworks.networkmonitor.model.NetworkType
import com.summitcodeworks.networkmonitor.model.WebSocketEvent
import com.summitcodeworks.networkmonitor.model.WebSocketEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.*
import javax.inject.Inject

class NetworkMonitorWebSocketListener @Inject constructor(
    private val networkLogDao: NetworkLogDao,
    private val webSocketEventDao: WebSocketEventDao,
    private val originalListener: WebSocketListener? = null
) : WebSocketListener() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val connectionId = UUID.randomUUID().toString()
    private var connectionStartTime: Long = 0
    private var url: String = ""

    override fun onOpen(webSocket: WebSocket, response: Response) {
        connectionStartTime = System.currentTimeMillis()
        url = response.request.url.toString()

        scope.launch {
            // Log the initial WebSocket connection with detailed headers
            val requestHeaders = response.request.headers.toMultimap().toString()
            val responseHeaders = response.headers.toMultimap().toString()
            
            val networkLog = NetworkLog(
                requestId = connectionId,
                type = NetworkType.WEBSOCKET,
                url = url,
                requestHeaders = requestHeaders,
                responseHeaders = responseHeaders,
                requestTime = connectionStartTime,
                responseCode = response.code,
                protocol = response.protocol.toString(),
                isSSL = response.request.url.isHttps
            )
            networkLogDao.insertLog(networkLog)

            // Log WebSocket event
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.OPEN,
                message = "WebSocket connection established with code: ${response.code}",
                timestamp = connectionStartTime
            )
            webSocketEventDao.insertEvent(event)
        }

        originalListener?.onOpen(webSocket, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val messageDetails = "Text message (${text.length} chars): $text"
            
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.MESSAGE_RECEIVED,
                message = messageDetails,
                timestamp = timestamp
            )
            webSocketEventDao.insertEvent(event)
        }

        originalListener?.onMessage(webSocket, text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val hexPreview = if (bytes.size <= 100) {
                bytes.hex()
            } else {
                bytes.substring(0, 50).hex() + "... (truncated)"
            }
            val messageDetails = "Binary message (${bytes.size} bytes): $hexPreview"
            
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.MESSAGE_RECEIVED,
                message = messageDetails,
                timestamp = timestamp
            )
            webSocketEventDao.insertEvent(event)
        }

        originalListener?.onMessage(webSocket, bytes)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        scope.launch {
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.CLOSING,
                message = "Code: $code, Reason: $reason",
                timestamp = System.currentTimeMillis()
            )
            webSocketEventDao.insertEvent(event)
        }

        originalListener?.onClosing(webSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        val closeTime = System.currentTimeMillis()
        val duration = closeTime - connectionStartTime

        scope.launch {
            // Update the network log with close time and duration
            val existingLogs = networkLogDao.getAllLogs()
            // Find and update the log for this connection
            // Simplified approach - in real implementation, you'd want to store the log ID

            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.CLOSED,
                message = "Code: $code, Reason: $reason",
                timestamp = closeTime
            )
            webSocketEventDao.insertEvent(event)
        }

        originalListener?.onClosed(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        val failureTime = System.currentTimeMillis()

        scope.launch {
            // Update the network log with error
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.FAILURE,
                error = t.message,
                timestamp = failureTime
            )
            webSocketEventDao.insertEvent(event)
        }

        originalListener?.onFailure(webSocket, t, response)
    }

    fun logSentMessage(message: String) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val messageDetails = "Text message sent (${message.length} chars): $message"
            
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.MESSAGE_SENT,
                message = messageDetails,
                timestamp = timestamp
            )
            webSocketEventDao.insertEvent(event)
        }
    }

    fun logSentMessage(bytes: ByteString) {
        scope.launch {
            val timestamp = System.currentTimeMillis()
            val hexPreview = if (bytes.size <= 100) {
                bytes.hex()
            } else {
                bytes.substring(0, 50).hex() + "... (truncated)"
            }
            val messageDetails = "Binary message sent (${bytes.size} bytes): $hexPreview"
            
            val event = WebSocketEvent(
                connectionId = connectionId,
                url = url,
                eventType = WebSocketEventType.MESSAGE_SENT,
                message = messageDetails,
                timestamp = timestamp
            )
            webSocketEventDao.insertEvent(event)
        }
    }
}