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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.summitcodeworks.chitchat.presentation.components.UserProfileBottomSheet
import androidx.compose.foundation.clickable
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Chat screen composable for one-on-one conversations in ChitChat.
 * 
 * This screen provides a complete chat interface for direct messaging including:
 * - Real-time message display with proper styling (sent vs received)
 * - Message input with text and media support
 * - Typing indicators for real-time user feedback
 * - Message timestamps and read receipts
 * - Media picker for sharing images and files
 * - User profile access from chat header
 * - Automatic message loading and pagination
 * 
 * The chat screen integrates with WebSocket for real-time updates and manages:
 * - Message sending and receiving
 * - Typing indicator synchronization
 * - Conversation read status updates
 * - Media message handling
 * - User profile information display
 * 
 * Key features:
 * - Real-time messaging via WebSocket
 * - Message bubble styling with timestamps
 * - Typing indicators and online status
 * - Media sharing capabilities
 * - User profile bottom sheet
 * - Automatic scrolling to latest messages
 * - Message state management (sending, sent, delivered)
 * 
 * @param userId ID of the user to chat with
 * @param onNavigateBack Callback to navigate back to conversations list
 * @param chatViewModel ViewModel handling chat logic and message state
 * @param otpAuthManager Authentication manager for current user identification
 * @param conversationsViewModel ViewModel for updating conversation list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    otpAuthManager: OtpAuthManager = hiltViewModel<com.summitcodeworks.chitchat.presentation.viewmodel.HomeScreenAuthViewModel>().otpAuthManager,
    conversationsViewModel: com.summitcodeworks.chitchat.presentation.viewmodel.ConversationsViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }
    var showUserProfile by remember { mutableStateOf(false) }
    val chatState by chatViewModel.chatState.collectAsState()
    val otherUserTyping by chatViewModel.isOtherUserTyping.collectAsState()
    val otherUserName by chatViewModel.otherUserName.collectAsState()
    val otherUserPhone by chatViewModel.otherUserPhone.collectAsState()
    val otherUserDetails by chatViewModel.otherUserDetails.collectAsState()
    val listState = rememberLazyListState()
    val currentToken by otpAuthManager.currentToken.collectAsState()
    val currentUser by otpAuthManager.currentUser.collectAsState()
    
    // Typing indicator state management
    var typingJob by remember { mutableStateOf<Job?>(null) }
    var isCurrentlyTyping by remember { mutableStateOf(false) }
    
    // Check if the OTHER user (not current user) is typing
    val isOtherPersonTyping = otherUserTyping.first == userId && otherUserTyping.second
    
    // Display name: priority - name > phone number > user ID
    val displayName = otherUserName ?: otherUserPhone ?: "$userId"
    
    // Load user name and messages when screen opens
    LaunchedEffect(userId, currentToken) {
        chatViewModel.loadUserName(userId)
        chatViewModel.setCurrentConversation(userId)
        
        // Clear unread count for this conversation
        conversationsViewModel.clearUnreadCount(userId)
        
        // Load initial messages from API
        currentToken?.let { token ->
            chatViewModel.loadConversationMessages(token, userId)
        }
    }
    
    // Observe local messages from database in real-time
    LaunchedEffect(userId, currentUser) {
        currentUser?.let { user ->
            chatViewModel.observeLocalMessages(user.id, userId)
        }
    }
    
    // Stop typing indicator and clear conversation when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            if (isCurrentlyTyping) {
                chatViewModel.sendTypingIndicator(userId, false)
            }
            chatViewModel.setCurrentConversation(0) // Clear current conversation
        }
    }
    
    LaunchedEffect(chatState.messages) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showUserProfile = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = displayName,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            // Online status
                            Text(
                                text = if (otherUserDetails?.isOnline == true) "online" else "offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
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
                            isOwnMessage = currentUser?.id == message.senderId
                        )
                    }

                    // Typing indicator - only show when OTHER person is typing
                    item {
                        TypingIndicator(
                            isVisible = isOtherPersonTyping,
                            userName = displayName
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
                            
                            // WhatsApp-like typing indicator logic
                            if (it.isNotBlank()) {
                                // Cancel existing typing timeout
                                typingJob?.cancel()
                                
                                // If not already typing, notify the receiver
                                if (!isCurrentlyTyping) {
                                    isCurrentlyTyping = true
                                    chatViewModel.sendTypingIndicator(userId, true)
                                }
                                
                                // Set a new timeout to stop typing indicator after 3 seconds of inactivity
                                typingJob = CoroutineScope(Dispatchers.Main).launch {
                                    delay(3000) // 3 seconds timeout
                                    isCurrentlyTyping = false
                                    chatViewModel.sendTypingIndicator(userId, false)
                                }
                            } else {
                                // Text is empty, stop typing indicator immediately
                                typingJob?.cancel()
                                if (isCurrentlyTyping) {
                                    isCurrentlyTyping = false
                                    chatViewModel.sendTypingIndicator(userId, false)
                                }
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
                                // Cancel typing timeout and stop typing indicator
                                typingJob?.cancel()
                                if (isCurrentlyTyping) {
                                    isCurrentlyTyping = false
                                    chatViewModel.sendTypingIndicator(userId, false)
                                }
                                
                                chatViewModel.sendMessage(
                                    token = currentToken!!,
                                    receiverId = userId,
                                    content = messageText,
                                    messageType = MessageType.TEXT
                                )
                                messageText = ""
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
            }

            // Loading indicator overlay
            if (chatState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message overlay
            chatState.error?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .imePadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Having trouble connecting. Pull to refresh.",
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
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
        
        // User profile bottom sheet
        if (showUserProfile) {
            UserProfileBottomSheet(
                user = otherUserDetails,
                phoneNumber = otherUserPhone,
                onDismiss = { showUserProfile = false },
                onBlock = {
                    // TODO: Block user
                },
                onReport = {
                    // TODO: Report user
                },
                onViewMedia = {
                    // TODO: Navigate to media gallery
                },
                onVoiceCall = {
                    // TODO: Initiate voice call
                },
                onVideoCall = {
                    // TODO: Initiate video call
                }
            )
        }
    }
}

