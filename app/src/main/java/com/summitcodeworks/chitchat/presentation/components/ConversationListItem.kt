package com.summitcodeworks.chitchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.summitcodeworks.chitchat.domain.model.Conversation

@Composable
fun ConversationListItem(
    conversation: Conversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                conversation.lastMessageTime?.let { time ->
                    Text(
                        text = formatTime(time),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage ?: "Tap to start chatting",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (conversation.lastMessage != null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: String): String {
    return try {
        val messageTime = java.time.Instant.parse(timestamp).toEpochMilli()
        val now = System.currentTimeMillis()
        val diff = now - messageTime
        
        when {
            diff < 60000 -> "Just now" // Less than 1 minute
            diff < 3600000 -> "${diff / 60000}m" // Less than 1 hour
            diff < 86400000 -> { // Less than 24 hours
                val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                formatter.format(java.util.Date(messageTime))
            }
            diff < 172800000 -> "Yesterday" // Less than 2 days
            diff < 604800000 -> { // Less than 7 days
                val formatter = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
                formatter.format(java.util.Date(messageTime))
            }
            else -> { // Older than 7 days
                val formatter = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.getDefault())
                formatter.format(java.util.Date(messageTime))
            }
        }
    } catch (e: Exception) {
        // Fallback for parsing errors
        try {
            val date = java.util.Date(timestamp.toLongOrNull() ?: 0L)
            val formatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}
