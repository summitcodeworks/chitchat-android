package com.summitcodeworks.chitchat.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.chitchat.presentation.viewmodel.HomeViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.HomeScreenAuthViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.EnvironmentViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.ConversationsViewModel
import com.summitcodeworks.chitchat.data.config.Environment
import com.summitcodeworks.chitchat.presentation.components.NewChatBottomSheet
import com.summitcodeworks.chitchat.presentation.components.ConversationListItem
import com.summitcodeworks.chitchat.domain.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: (Long) -> Unit,
    onNavigateToContactPicker: () -> Unit = {},
    onNavigateToStatusCamera: () -> Unit = {},
    onNavigateToCallContacts: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    homeScreenAuthViewModel: HomeScreenAuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    environmentViewModel: EnvironmentViewModel = hiltViewModel(),
    conversationsViewModel: ConversationsViewModel = hiltViewModel()
) {
    val isAuthenticated by homeScreenAuthViewModel.otpAuthManager.isAuthenticated.collectAsState()
    val currentUser by homeScreenAuthViewModel.otpAuthManager.currentUser.collectAsState()
    val currentToken by homeScreenAuthViewModel.otpAuthManager.currentToken.collectAsState()
    val user by homeViewModel.user.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val error by homeViewModel.error.collectAsStateWithLifecycle()
    val currentEnvironment by environmentViewModel.currentEnvironment.collectAsStateWithLifecycle()
    
    // WebSocket manager
    val webSocketManager = hiltViewModel<com.summitcodeworks.chitchat.presentation.viewmodel.WebSocketViewModel>()

    // Authentication guard - redirect to auth if not properly authenticated
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            onNavigateToAuth()
        }
    }
    
    // Connect WebSocket when authenticated
    LaunchedEffect(isAuthenticated, currentToken) {
        if (isAuthenticated) {
            currentToken?.let { token ->
                webSocketManager.connectWebSocket(token)
            }
        }
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showEnvironmentDialog by remember { mutableStateOf(false) }
    var showNewChatBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ChitChat",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { 
                            android.util.Log.d("HomeScreen", "Search button clicked")
                            onNavigateToSearch()
                        }
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(onClick = {
                            showDropdownMenu = true
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Environment: ${currentEnvironment.displayName}") },
                                onClick = {
                                    showDropdownMenu = false
                                    showEnvironmentDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Computer, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Debug Menu") },
                                onClick = {
                                    showDropdownMenu = false
                                    onNavigateToDebug()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.BugReport, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showDropdownMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sign Out") },
                                onClick = {
                                    showDropdownMenu = false
                                    homeScreenAuthViewModel.otpAuthManager.signOut()
                                    onNavigateToAuth()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTabIndex) {
                0 -> { // Chats tab
                    FloatingActionButton(
                        onClick = { showNewChatBottomSheet = true }
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "New Chat")
                    }
                }
                1 -> { // Status tab
                    FloatingActionButton(
                        onClick = onNavigateToStatusCamera
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Add Status")
                    }
                }
                2 -> { // Calls tab
                    FloatingActionButton(
                        onClick = onNavigateToCallContacts
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "New Call")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Tab Row with chip/pill styling like network monitor
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                },
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier,
                        height = 0.dp,
                        color = Color.Transparent
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Chats",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTabIndex == 0)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 1.dp, vertical = 4.dp)
                        .background(
                            color = if (selectedTabIndex == 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Status",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTabIndex == 1)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 1.dp, vertical = 4.dp)
                        .background(
                            color = if (selectedTabIndex == 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            "Calls",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTabIndex == 2) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTabIndex == 2)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 1.dp, vertical = 4.dp)
                        .background(
                            color = if (selectedTabIndex == 2)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ChatsTab(
                    onNavigateToChat = onNavigateToChat,
                    conversationsViewModel = conversationsViewModel
                )
                1 -> StatusTab()
                2 -> CallsTab()
            }
        }
    }

    // Environment Selection Dialog
    if (showEnvironmentDialog) {
        EnvironmentSelectionDialog(
            currentEnvironment = currentEnvironment,
            onEnvironmentSelected = { environment ->
                environmentViewModel.setEnvironment(environment)
                showEnvironmentDialog = false
            },
            onDismiss = { showEnvironmentDialog = false }
        )
    }

    // New Chat Bottom Sheet
    if (showNewChatBottomSheet) {
        NewChatBottomSheet(
            onDismiss = { showNewChatBottomSheet = false },
            onNewGroupChat = {
                // TODO: Navigate to new group chat creation
            },
            onNewBroadcast = {
                // TODO: Navigate to new broadcast creation
            },
            onLinkedDevices = {
                // TODO: Navigate to linked devices screen
            },
            onStarredMessages = {
                // TODO: Navigate to starred messages screen
            },
            onSettings = {
                // TODO: Navigate to settings screen
            },
            onContactSelected = { contact ->
                // Add conversation to the list if not exists
                val userName = contact.registeredUser?.name ?: contact.name
                val userAvatar = contact.registeredUser?.avatarUrl
                conversationsViewModel.addConversationIfNotExists(
                    userId = contact.id,
                    userName = userName,
                    userAvatar = userAvatar
                )
                // Navigate to chat with selected contact
                showNewChatBottomSheet = false
                onNavigateToChat(contact.id)
            }
        )
    }
}

@Composable
fun ChatsTab(
    onNavigateToChat: (Long) -> Unit,
    conversationsViewModel: ConversationsViewModel = hiltViewModel()
) {
    val conversations by conversationsViewModel.conversations.collectAsState()
    
    if (conversations.isEmpty()) {
        EmptyStateContent(
            icon = Icons.Default.Chat,
            title = "No chats yet",
            description = "Start a new conversation to see your chats here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(conversations) { conversation ->
                ConversationListItem(
                    conversation = conversation,
                    onClick = {
                        onNavigateToChat(conversation.userId)
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 84.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun StatusTab() {
    EmptyStateContent(
        icon = Icons.Default.Refresh,
        title = "No status updates",
        description = "Share your moments with status updates"
    )
}

@Composable
fun CallsTab() {
    EmptyStateContent(
        icon = Icons.Default.Call,
        title = "No recent calls",
        description = "Your call history will appear here"
    )
}

@Composable
fun EmptyStateContent(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun EnvironmentSelectionDialog(
    currentEnvironment: Environment,
    onEnvironmentSelected: (Environment) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Environment")
        },
        text = {
            Column {
                Text(
                    text = "Choose the environment for API calls:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Environment.values().forEach { environment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentEnvironment == environment,
                            onClick = { onEnvironmentSelected(environment) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = environment.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = environment.apiBaseUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
