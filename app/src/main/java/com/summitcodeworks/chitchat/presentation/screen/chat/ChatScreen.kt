package com.summitcodeworks.chitchat.presentation.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.summitcodeworks.chitchat.domain.model.Message
import com.summitcodeworks.chitchat.domain.model.MessageType
import com.summitcodeworks.chitchat.presentation.state.ChatState
import com.summitcodeworks.chitchat.presentation.viewmodel.ChatViewModel
// AuthViewModel removed - using OTP authentication
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import com.summitcodeworks.chitchat.presentation.components.MessageBubble
import com.summitcodeworks.chitchat.presentation.components.TypingIndicator
import com.summitcodeworks.chitchat.presentation.components.MediaPickerBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    otpAuthManager: OtpAuthManager = hiltViewModel<com.summitcodeworks.chitchat.presentation.viewmodel.HomeScreenAuthViewModel>().otpAuthManager
) {
    var messageText by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    val chatState by chatViewModel.chatState.collectAsState()
    val listState = rememberLazyListState()
    val currentToken by otpAuthManager.currentToken.collectAsState()
    
    LaunchedEffect(chatState.messages) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat with User $userId",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // TODO: Initiate voice call
                        // Could navigate to call screen or initiate call directly
                    }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call")
                    }
                    IconButton(onClick = { 
                        // TODO: Initiate video call
                        // Could navigate to call screen or initiate video call directly
                    }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call")
                    }
                    IconButton(onClick = { 
                        // TODO: Show more options menu
                        // Could show bottom sheet with options like view profile, block user, etc.
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(chatState.messages) { message ->
                    MessageBubble(
                        message = message,
                        isOwnMessage = message.senderId == 1L // Mock current user ID
                    )
                }
                
                // Typing indicator
                item {
                    TypingIndicator(
                        isVisible = isTyping,
                        userName = "User $userId"
                    )
                }
            }
            
            // Message Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment button
                IconButton(
                    onClick = { showMediaPicker = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Attach",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { 
                        messageText = it
                        // Simulate typing indicator
                        if (it.isNotBlank() && !isTyping) {
                            isTyping = true
                        } else if (it.isBlank() && isTyping) {
                            isTyping = false
                        }
                    },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )
                
                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank() && currentToken != null) {
                            chatViewModel.sendMessage(
                                token = currentToken!!,
                                receiverId = userId,
                                content = messageText,
                                messageType = MessageType.TEXT
                            )
                            messageText = ""
                            isTyping = false
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            // Loading indicator
            if (chatState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Error message
            chatState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    
    // Media picker bottom sheet
    if (showMediaPicker) {
        MediaPickerBottomSheet(
            onDismiss = { showMediaPicker = false },
            onImageSelected = {
                showMediaPicker = false
                // TODO: Upload image and send message
                // chatViewModel.sendMessage("token", userId, null, "", MessageType.IMAGE, null, mediaId)
            },
            onVideoSelected = {
                showMediaPicker = false
                // TODO: Upload video and send message
                // chatViewModel.sendMessage("token", userId, null, "", MessageType.VIDEO, null, mediaId)
            },
            onDocumentSelected = {
                showMediaPicker = false
                // TODO: Upload document and send message
                // chatViewModel.sendMessage("token", userId, null, "", MessageType.DOCUMENT, null, mediaId)
            },
            onCameraSelected = { 
                showMediaPicker = false
                // TODO: Open camera to capture image/video
                // Could use CameraX or system camera intent
            }
        )
    }
}

