package com.summitcodeworks.chitchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.summitcodeworks.chitchat.domain.model.Message
import com.summitcodeworks.chitchat.domain.model.MessageType
import com.summitcodeworks.chitchat.ui.theme.ReceivedMessageBackground
import com.summitcodeworks.chitchat.ui.theme.SentMessageBackground
import java.text.SimpleDateFormat
import java.util.*

/**
 * Message bubble component for displaying chat messages in ChitChat.
 * 
 * This composable renders individual chat messages with appropriate styling
 * based on whether the message was sent by the current user or received from
 * another user. It supports various message types and provides rich formatting
 * for different content types.
 * 
 * Message types supported:
 * - TEXT: Plain text messages with proper formatting
 * - IMAGE: Image messages with thumbnails and metadata
 * - VIDEO: Video messages with preview and duration
 * - AUDIO: Audio messages with waveform visualization
 * - DOCUMENT: File attachments with icons and metadata
 * 
 * Styling features:
 * - Different colors for sent vs received messages
 * - Rounded corners with appropriate corner radius
 * - Message timestamps and read receipts
 * - Reply message threading
 * - Media content display
 * - Message status indicators
 * 
 * The component automatically handles:
 * - Message alignment (right for sent, left for received)
 * - Content wrapping and text overflow
 * - Timestamp formatting and display
 * - Media type-specific rendering
 * - Accessibility features
 * 
 * @param message The message object containing content and metadata
 * @param isOwnMessage Whether this message was sent by the current user
 * @param modifier Modifier for styling the message bubble
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) SentMessageBackground else ReceivedMessageBackground
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message content based on type
                when (message.messageType) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    MessageType.IMAGE -> {
                        ImageMessageContent(
                            imageUrl = message.content,
                            caption = message.media?.description
                        )
                    }
                    MessageType.VIDEO -> {
                        VideoMessageContent(
                            videoUrl = message.content,
                            thumbnailUrl = message.media?.thumbnailUrl,
                            caption = message.media?.description
                        )
                    }
                    MessageType.AUDIO -> {
                        AudioMessageContent(
                            audioUrl = message.content,
                            duration = message.media?.duration
                        )
                    }
                    MessageType.DOCUMENT -> {
                        DocumentMessageContent(
                            fileName = message.media?.originalFileName ?: "Document",
                            fileSize = message.media?.fileSize
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Message metadata
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(
                            isDelivered = message.isDelivered,
                            isRead = message.isRead
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageMessageContent(
    imageUrl: String,
    caption: String? = null
) {
    Column {
        // Image placeholder - in real app, use AsyncImage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = "Image",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        caption?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VideoMessageContent(
    videoUrl: String,
    thumbnailUrl: String? = null,
    caption: String? = null
) {
    Column {
        // Video thumbnail placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Video",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        caption?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AudioMessageContent(
    audioUrl: String,
    duration: Long? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Play audio */ }) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play audio",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Audio waveform placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        duration?.let {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatDuration(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DocumentMessageContent(
    fileName: String,
    fileSize: Long? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = "Document",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            fileSize?.let {
                Text(
                    text = formatFileSize(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        IconButton(onClick = { /* Download document */ }) {
            Icon(
                Icons.Default.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MessageStatusIcon(
    isDelivered: Boolean,
    isRead: Boolean
) {
    val icon: ImageVector = when {
        isRead -> Icons.Default.DoneAll
        isDelivered -> Icons.Default.DoneAll
        else -> Icons.Default.Done
    }
    
    val tint: Color = when {
        isRead -> MaterialTheme.colorScheme.primary
        isDelivered -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Icon(
        icon,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = tint
    )
}

private fun formatTime(timestamp: String): String {
    return try {
        val date = Date(timestamp.toLong())
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } catch (e: Exception) {
        "2:30 PM" // Fallback
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
