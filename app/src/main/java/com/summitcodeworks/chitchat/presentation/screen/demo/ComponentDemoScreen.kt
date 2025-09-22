package com.summitcodeworks.chitchat.presentation.screen.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.summitcodeworks.chitchat.domain.model.*
import com.summitcodeworks.chitchat.presentation.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDemoScreen(
    modifier: Modifier = Modifier
) {
    var showMediaPicker by remember { mutableStateOf(false) }
    var showCreateStatus by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ChitChat Components Demo",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        // TODO: Show demo settings
                        // Could show settings for the demo screen
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Profile Avatars",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileAvatar(
                        imageUrl = null,
                        name = "John Doe",
                        size = 48.dp,
                        isOnline = true
                    )
                    
                    ProfileAvatar(
                        imageUrl = null,
                        name = "Jane Smith",
                        size = 64.dp,
                        isOnline = false
                    )
                    
                    StatusAvatar(
                        imageUrl = null,
                        name = "Mike Johnson",
                        hasNewStatus = true
                    )
                }
            }
            
            item {
                Text(
                    text = "Message Bubbles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(getDemoMessages()) { message ->
                MessageBubble(
                    message = message,
                    isOwnMessage = message.senderId == 1L
                )
            }
            
            item {
                Text(
                    text = "Status Cards",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(getDemoStatuses()) { status ->
                StatusCard(
                    status = status,
                    onClick = { 
                        // TODO: View status in demo
                        // Could show status details in demo mode
                    }
                )
            }
            
            item {
                Text(
                    text = "Call Cards",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(getDemoCalls()) { call ->
                CallCard(
                    call = call,
                    onClick = { 
                        // TODO: View call details in demo
                        // Could show call details in demo mode
                    },
                    onCallBack = { 
                        // TODO: Call back in demo
                        // Could simulate call back in demo mode
                    }
                )
            }
            
            item {
                Text(
                    text = "Interactive Components",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Typing indicator
                    TypingIndicator(
                        isVisible = isTyping,
                        userName = "John Doe"
                    )
                    
                    // Toggle typing indicator
                    Button(
                        onClick = { isTyping = !isTyping }
                    ) {
                        Text(if (isTyping) "Hide Typing" else "Show Typing")
                    }
                    
                    // Media picker trigger
                    Button(
                        onClick = { showMediaPicker = true }
                    ) {
                        Text("Show Media Picker")
                    }
                    
                    // Status creator trigger
                    Button(
                        onClick = { showCreateStatus = true }
                    ) {
                        Text("Create Status")
                    }
                    
                    // Call action buttons
                    CallActionButtons(
                        onVoiceCall = { 
                            // TODO: Voice call in demo
                            // Could simulate voice call in demo mode
                        },
                        onVideoCall = { 
                            // TODO: Video call in demo
                            // Could simulate video call in demo mode
                        }
                    )
                }
            }
            
            item {
                Text(
                    text = "Connection Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ConnectionStatusIndicator(isConnected = true)
                    ConnectionStatusIndicator(isConnected = false)
                }
            }
        }
    }
    
    // Media picker bottom sheet
    if (showMediaPicker) {
        MediaPickerBottomSheet(
            onDismiss = { showMediaPicker = false },
            onImageSelected = { showMediaPicker = false },
            onVideoSelected = { showMediaPicker = false },
            onDocumentSelected = { showMediaPicker = false },
            onCameraSelected = { showMediaPicker = false }
        )
    }
    
    // Create status bottom sheet
    if (showCreateStatus) {
        CreateStatusBottomSheet(
            onDismiss = { showCreateStatus = false },
            onStatusCreated = { showCreateStatus = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateStatusBottomSheet(
    onDismiss: () -> Unit,
    onStatusCreated: () -> Unit
) {
    var statusText by remember { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Create Status",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = statusText,
                onValueChange = { statusText = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = onStatusCreated,
                    modifier = Modifier.weight(1f),
                    enabled = statusText.isNotBlank()
                ) {
                    Text("Post")
                }
            }
        }
    }
}

private fun getDemoMessages(): List<Message> = listOf(
    Message(
        id = "1",
        senderId = 1,
        receiverId = 2,
        content = "Hello! How are you doing?",
        messageType = MessageType.TEXT,
        timestamp = System.currentTimeMillis().toString(),
        isRead = true,
        isDelivered = true
    ),
    Message(
        id = "2",
        senderId = 2,
        receiverId = 1,
        content = "I'm doing great! Thanks for asking. How about you?",
        messageType = MessageType.TEXT,
        timestamp = (System.currentTimeMillis() + 60000).toString(),
        isRead = true,
        isDelivered = true
    ),
    Message(
        id = "3",
        senderId = 1,
        receiverId = 2,
        content = "image_url_here",
        messageType = MessageType.IMAGE,
        timestamp = (System.currentTimeMillis() + 120000).toString(),
        isRead = false,
        isDelivered = true,
        media = Media(
            id = 1,
            fileName = "photo.jpg",
            originalFileName = "my_photo.jpg",
            fileSize = 1024000,
            mediaType = MediaType.IMAGE,
            mimeType = "image/jpeg",
            url = "image_url_here",
            description = "Beautiful sunset at the beach",
            uploadedBy = 1,
            uploadedAt = System.currentTimeMillis().toString()
        )
    ),
    Message(
        id = "4",
        senderId = 2,
        receiverId = 1,
        content = "document.pdf",
        messageType = MessageType.DOCUMENT,
        timestamp = (System.currentTimeMillis() + 180000).toString(),
        isRead = false,
        isDelivered = false,
        media = Media(
            id = 2,
            fileName = "document.pdf",
            originalFileName = "important_document.pdf",
            fileSize = 2048000,
            mediaType = MediaType.DOCUMENT,
            mimeType = "application/pdf",
            url = "document.pdf",
            description = "Important document",
            uploadedBy = 2,
            uploadedAt = System.currentTimeMillis().toString()
        )
    )
)

private fun getDemoStatuses(): List<Status> = listOf(
    Status(
        id = 1,
        userId = 2,
        content = "Having a wonderful day at the beach! 🌊☀️",
        statusType = StatusType.TEXT,
        backgroundColor = "#FF6B6B",
        createdAt = System.currentTimeMillis().toString(),
        expiresAt = (System.currentTimeMillis() + 86400000).toString(),
        viewCount = 25,
        reactionCount = 12,
        user = User(
            id = 2,
            phoneNumber = "+1234567890",
            name = "John Doe",
            isOnline = true
        )
    ),
    Status(
        id = 2,
        userId = 3,
        content = "image_url",
        statusType = StatusType.IMAGE,
        createdAt = (System.currentTimeMillis() - 3600000).toString(),
        expiresAt = (System.currentTimeMillis() + 82800000).toString(),
        viewCount = 18,
        reactionCount = 8,
        user = User(
            id = 3,
            phoneNumber = "+1234567891",
            name = "Jane Smith",
            isOnline = false
        ),
        media = Media(
            id = 3,
            fileName = "status_image.jpg",
            originalFileName = "beach_photo.jpg",
            fileSize = 1536000,
            mediaType = MediaType.IMAGE,
            mimeType = "image/jpeg",
            url = "image_url",
            description = "Beautiful beach view",
            uploadedBy = 3,
            uploadedAt = System.currentTimeMillis().toString()
        )
    )
)

private fun getDemoCalls(): List<Call> = listOf(
    Call(
        sessionId = "call_1",
        callerId = 1,
        calleeId = 2,
        callType = CallType.VOICE,
        status = CallStatus.ANSWERED,
        startTime = System.currentTimeMillis().toString(),
        endTime = (System.currentTimeMillis() + 300000).toString(),
        duration = 300,
        callee = User(
            id = 2,
            phoneNumber = "+1234567890",
            name = "John Doe"
        )
    ),
    Call(
        sessionId = "call_2",
        callerId = 3,
        calleeId = 1,
        callType = CallType.VIDEO,
        status = CallStatus.MISSED,
        startTime = (System.currentTimeMillis() - 3600000).toString(),
        callee = User(
            id = 3,
            phoneNumber = "+1234567891",
            name = "Jane Smith"
        )
    ),
    Call(
        sessionId = "call_3",
        callerId = 1,
        calleeId = 4,
        callType = CallType.VOICE,
        status = CallStatus.REJECTED,
        startTime = (System.currentTimeMillis() - 7200000).toString(),
        callee = User(
            id = 4,
            phoneNumber = "+1234567892",
            name = "Mike Johnson"
        )
    )
)
