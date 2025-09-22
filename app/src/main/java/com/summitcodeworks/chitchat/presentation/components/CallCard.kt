package com.summitcodeworks.chitchat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.summitcodeworks.chitchat.domain.model.Call
import com.summitcodeworks.chitchat.domain.model.CallStatus
import com.summitcodeworks.chitchat.domain.model.CallType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallCard(
    call: Call,
    onClick: () -> Unit,
    onCallBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call type icon
            CallTypeIcon(
                callType = call.callType,
                callStatus = call.status,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Call info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = call.callee?.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCallStatus(call.status),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getStatusColor(call.status)
                    )
                    
                    call.duration?.let { duration ->
                        Text(
                            text = " • ${formatDuration(duration)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Time and action
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCallTime(call.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                IconButton(
                    onClick = onCallBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        getCallBackIcon(call.callType),
                        contentDescription = "Call back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun CallTypeIcon(
    callType: CallType,
    callStatus: CallStatus,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (callType) {
        CallType.VOICE -> Icons.Default.Call
        CallType.VIDEO -> Icons.Default.VideoCall
    }
    
    val backgroundColor = when (callStatus) {
        CallStatus.ANSWERED -> Color.Green
        CallStatus.MISSED -> Color.Red
        CallStatus.REJECTED -> Color(0xFFFF9800)
        CallStatus.ENDED -> Color.Gray
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .padding(6.dp)
                .size(12.dp),
            tint = Color.White
        )
    }
}

@Composable
fun CallHistoryCard(
    call: Call,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                imageUrl = call.callee?.avatarUrl,
                name = call.callee?.name ?: "Unknown",
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = call.callee?.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = formatCallTime(call.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallTypeIcon(
                    callType = call.callType,
                    callStatus = call.status,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                call.duration?.let { duration ->
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CallActionButtons(
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CallActionButton(
            icon = Icons.Default.Call,
            label = "Voice",
            onClick = onVoiceCall,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        CallActionButton(
            icon = Icons.Default.VideoCall,
            label = "Video",
            onClick = onVideoCall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActiveCallCard(
    call: Call,
    onEndCall: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileAvatar(
                imageUrl = call.callee?.avatarUrl,
                name = call.callee?.name ?: "Unknown",
                size = 120.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = call.callee?.name ?: "Unknown",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = formatCallStatus(call.status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            call.duration?.let { duration ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onMute,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Mute",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                IconButton(
                    onClick = onSpeaker,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Speaker",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                FloatingActionButton(
                    onClick = onEndCall,
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "End call",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

private fun formatCallStatus(status: CallStatus): String {
    return when (status) {
        CallStatus.INITIATED -> "Calling..."
        CallStatus.RINGING -> "Ringing..."
        CallStatus.ANSWERED -> "Connected"
        CallStatus.REJECTED -> "Declined"
        CallStatus.ENDED -> "Ended"
        CallStatus.MISSED -> "Missed call"
    }
}

@Composable
private fun getStatusColor(status: CallStatus): Color {
    return when (status) {
        CallStatus.ANSWERED -> Color.Green
        CallStatus.MISSED -> Color.Red
        CallStatus.REJECTED -> Color(0xFFFF9800)
        CallStatus.ENDED -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun getCallBackIcon(callType: CallType): ImageVector {
    return when (callType) {
        CallType.VOICE -> Icons.Default.Call
        CallType.VIDEO -> Icons.Default.VideoCall
    }
}

private fun formatCallTime(timestamp: String): String {
    return try {
        val date = Date(timestamp.toLong())
        val now = Date()
        val diff = now.time - date.time
        
        when {
            diff < 86400000 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        "2:30 PM" // Fallback
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format("%d:%02d", minutes, remainingSeconds)
    }
}
