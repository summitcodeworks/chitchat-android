@file:OptIn(ExperimentalMaterial3Api::class)
package com.summitcodeworks.chitchat.presentation.screen.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.chitchat.domain.model.Call
import com.summitcodeworks.chitchat.domain.model.CallStatus
import com.summitcodeworks.chitchat.domain.model.CallType
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.presentation.components.CallCard
import com.summitcodeworks.chitchat.presentation.components.CallHistoryCard
import com.summitcodeworks.chitchat.presentation.components.CallActionButtons
import com.summitcodeworks.chitchat.presentation.viewmodel.CallsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    modifier: Modifier = Modifier,
    viewModel: CallsViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Missed")
    
    val calls by viewModel.calls.collectAsStateWithLifecycle()
    val currentCall by viewModel.currentCall.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load call history on first composition
    LaunchedEffect(Unit) {
        // Assuming we have a token from somewhere (AuthViewModel or similar)
        // viewModel.loadCallHistory("token")
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calls",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        // TODO: Implement search functionality
                        // Could open a search dialog or navigate to search screen
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { 
                        // TODO: Implement call settings
                        // Could open settings dialog or navigate to settings screen
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // TODO: Implement new call functionality
                    // Could open a contact picker or dialer
                }
            ) {
                Icon(Icons.Default.Call, contentDescription = "New Call")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Call List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredCalls = if (selectedTab == 0) {
                    calls
                } else {
                    calls.filter { it.status == CallStatus.MISSED }
                }
                
                items(filteredCalls) { call ->
                    CallCard(
                        call = call,
                        onClick = { 
                            // TODO: Navigate to call details screen
                        },
                        onCallBack = { 
                            // TODO: Initiate call back
                            // viewModel.initiateCall("token", call.calleeId, null, CallType.VOICE)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CallHistoryScreen(
    userId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallsViewModel = hiltViewModel()
) {
    val calls by viewModel.calls.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Call History",
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
                        // TODO: Implement clear history functionality
                        // Could show confirmation dialog and then clear local call history
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // User info header
            CallUserHeader(userId = userId)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Call action buttons
            CallActionButtons(
                onVoiceCall = { 
                    // TODO: Initiate voice call
                    // viewModel.initiateCall("token", userId, null, CallType.VOICE)
                },
                onVideoCall = { 
                    // TODO: Initiate video call
                    // viewModel.initiateCall("token", userId, null, CallType.VIDEO)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Call history list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val userCalls = calls.filter { it.calleeId == userId || it.callerId == userId }
                items(userCalls) { call ->
                    CallHistoryCard(
                        call = call,
                        onClick = { 
                            // TODO: Navigate to call details screen
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallUserHeader(
    userId: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "User $userId",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Last seen recently",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActiveCallScreen(
    call: Call,
    onEndCall: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Call status card
                com.summitcodeworks.chitchat.presentation.components.ActiveCallCard(
                    call = call,
                    onEndCall = onEndCall,
                    onMute = {
                        isMuted = !isMuted
                        onMute()
                    },
                    onSpeaker = {
                        isSpeakerOn = !isSpeakerOn
                        onSpeaker()
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Additional call options
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CallOptionButton(
                        icon = Icons.Default.Mic,
                        isActive = !isMuted,
                        onClick = {
                            isMuted = !isMuted
                            onMute()
                        }
                    )
                    
                    CallOptionButton(
                        icon = Icons.Outlined.VolumeUp,
                        isActive = isSpeakerOn,
                        onClick = {
                            isSpeakerOn = !isSpeakerOn
                            onSpeaker()
                        }
                    )
                    
                    CallOptionButton(
                        icon = Icons.Default.Videocam,
                        isActive = false,
                        onClick = { 
                            // TODO: Switch to video call
                            // Could upgrade current call to video or start new video call
                        }
                    )
                    
                    CallOptionButton(
                        icon = Icons.Default.PersonAdd,
                        isActive = false,
                        onClick = { 
                            // TODO: Add participant to call
                            // Could open contact picker to add more people to the call
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isActive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun getAllMockCalls(): List<Call> = listOf(
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

private fun getMissedMockCalls(): List<Call> = listOf(
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
        sessionId = "call_4",
        callerId = 5,
        calleeId = 1,
        callType = CallType.VOICE,
        status = CallStatus.MISSED,
        startTime = (System.currentTimeMillis() - 10800000).toString(),
        callee = User(
            id = 5,
            phoneNumber = "+1234567893",
            name = "Sarah Wilson"
        )
    )
)

private fun getUserCallHistory(userId: Long): List<Call> = listOf(
    Call(
        sessionId = "call_1",
        callerId = 1,
        calleeId = userId,
        callType = CallType.VOICE,
        status = CallStatus.ANSWERED,
        startTime = System.currentTimeMillis().toString(),
        endTime = (System.currentTimeMillis() + 300000).toString(),
        duration = 300,
        caller = User(
            id = 1,
            phoneNumber = "+1234567890",
            name = "Me"
        )
    ),
    Call(
        sessionId = "call_2",
        callerId = userId,
        calleeId = 1,
        callType = CallType.VIDEO,
        status = CallStatus.ANSWERED,
        startTime = (System.currentTimeMillis() - 3600000).toString(),
        endTime = (System.currentTimeMillis() - 3300000).toString(),
        duration = 300,
        caller = User(
            id = userId,
            phoneNumber = "+1234567891",
            name = "User $userId"
        )
    )
)
