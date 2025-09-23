package com.summitcodeworks.chitchat.presentation.screen.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.chitchat.domain.model.Status
import com.summitcodeworks.chitchat.domain.model.StatusType
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.presentation.components.StatusCard
import com.summitcodeworks.chitchat.presentation.components.StatusPreviewCard
import com.summitcodeworks.chitchat.presentation.components.ProfileAvatar
import com.summitcodeworks.chitchat.presentation.components.StatusAvatar
import com.summitcodeworks.chitchat.presentation.viewmodel.StatusViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: StatusViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showCreateStatus by remember { mutableStateOf(false) }
    
    val userStatuses by viewModel.userStatuses.collectAsStateWithLifecycle()
    val activeStatuses by viewModel.activeStatuses.collectAsStateWithLifecycle()
    val contactsStatuses by viewModel.contactsStatuses.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    // Load statuses on first composition
    LaunchedEffect(authState.token) {
        authState.token?.let { token ->
            viewModel.loadActiveStatuses(token)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Status",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // TODO: Open camera for status
                        // Could open camera to capture image/video for status
                    }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
                    }
                    IconButton(onClick = { 
                        // TODO: Show status settings
                        // Could show settings like privacy, auto-delete, etc.
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateStatus = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Status")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // My Status Section
                MyStatusSection(
                    onStatusClick = {
                        showCreateStatus = true
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp))

                // Recent Updates Section
                RecentUpdatesSection(
                    activeStatuses = if (activeStatuses.isEmpty()) getMockStatuses() else activeStatuses
                )
            }

            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    
    if (showCreateStatus) {
        CreateStatusBottomSheet(
            onDismiss = { showCreateStatus = false },
            onStatusCreated = { showCreateStatus = false }
        )
    }
}

@Composable
private fun MyStatusSection(
    onStatusClick: () -> Unit
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
                .padding(16.dp)
                .clickable { onStatusClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusAvatar(
                imageUrl = null,
                name = "My Status",
                hasNewStatus = false
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "My Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Tap to add status update",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.Add,
                contentDescription = "Add status",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RecentUpdatesSection(activeStatuses: List<Status>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Recent Updates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(activeStatuses.size) { index ->
            val status = activeStatuses[index]
            StatusCard(
                status = status,
                onClick = { 
                    // TODO: Navigate to status view screen
                    // Could navigate to StatusViewScreen with the selected status
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusViewScreen(
    status: Status,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = status.user?.name ?: "Status",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // TODO: Show more options menu
                        // Could show options like share, report, block user, etc.
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
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
            StatusCard(
                status = status,
                onClick = { 
                    // TODO: Show full screen status view
                    // Could show full screen image/video viewer
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status reactions and views
            StatusInteractionSection(status = status)
        }
    }
}

@Composable
private fun StatusInteractionSection(
    status: Status
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusStatItem(
                    icon = Icons.Default.Visibility,
                    label = "Views",
                    count = status.viewCount
                )
                
                StatusStatItem(
                    icon = Icons.Default.Favorite,
                    label = "Reactions",
                    count = status.reactionCount
                )
                
                StatusStatItem(
                    icon = Icons.Default.Share,
                    label = "Shares",
                    count = 0
                )
            }
        }
    }
}

@Composable
private fun StatusStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStatusBottomSheet(
    onDismiss: () -> Unit,
    onStatusCreated: () -> Unit
) {
    var selectedType by remember { mutableStateOf(StatusType.TEXT) }
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
            
            // Status type selector
            StatusTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status input
            when (selectedType) {
                StatusType.TEXT -> {
                    OutlinedTextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        label = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
                StatusType.IMAGE -> {
                    // Image picker placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Select image",
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Select Image")
                        }
                    }
                }
                StatusType.VIDEO -> {
                    // Video picker placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = "Select video",
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Select Video")
                        }
                    }
                }
            }
            
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
                    enabled = statusText.isNotBlank() || selectedType != StatusType.TEXT
                ) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
private fun StatusTypeSelector(
    selectedType: StatusType,
    onTypeSelected: (StatusType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusType.values().forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = type.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}

private fun getMockStatuses(): List<Status> = listOf(
    Status(
        id = 1,
        userId = 2,
        content = "Having a great day at the beach! 🌊",
        statusType = StatusType.TEXT,
        createdAt = System.currentTimeMillis().toString(),
        expiresAt = (System.currentTimeMillis() + 86400000).toString(),
        viewCount = 15,
        reactionCount = 8,
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
        content = "Beautiful sunset today",
        statusType = StatusType.IMAGE,
        createdAt = (System.currentTimeMillis() - 3600000).toString(),
        expiresAt = (System.currentTimeMillis() + 82800000).toString(),
        viewCount = 23,
        reactionCount = 12,
        user = User(
            id = 3,
            phoneNumber = "+1234567891",
            name = "Jane Smith",
            isOnline = false
        )
    ),
    Status(
        id = 3,
        userId = 4,
        content = "Funny moment caught on video!",
        statusType = StatusType.VIDEO,
        createdAt = (System.currentTimeMillis() - 7200000).toString(),
        expiresAt = (System.currentTimeMillis() + 79200000).toString(),
        viewCount = 45,
        reactionCount = 25,
        user = User(
            id = 4,
            phoneNumber = "+1234567892",
            name = "Mike Johnson",
            isOnline = true
        )
    )
)
