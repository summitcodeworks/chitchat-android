package com.summitcodeworks.chitchat.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.chitchat.presentation.viewmodel.AuthViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.HomeViewModel
import com.summitcodeworks.chitchat.presentation.viewmodel.EnvironmentViewModel
import com.summitcodeworks.chitchat.data.config.Environment
import com.summitcodeworks.chitchat.presentation.components.NewChatBottomSheet
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
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    environmentViewModel: EnvironmentViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val user by homeViewModel.user.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val error by homeViewModel.error.collectAsStateWithLifecycle()
    val currentEnvironment by environmentViewModel.currentEnvironment.collectAsStateWithLifecycle()

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
                    IconButton(onClick = {
                        // TODO: Navigate to search screen
                        // Could navigate to a search screen for users, groups, messages
                    }) {
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

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { 
                        Text(
                            "Chats",
                            color = if (selectedTabIndex == 0) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { 
                        Text(
                            "Status",
                            color = if (selectedTabIndex == 1) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { 
                        Text(
                            "Calls",
                            color = if (selectedTabIndex == 2) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ChatsTab(onNavigateToChat = onNavigateToChat)
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
                // Navigate to chat with selected contact
                onNavigateToChat(contact.id)
            }
        )
    }
}

@Composable
fun ChatsTab(onNavigateToChat: (Long) -> Unit) {
    EmptyStateContent(
        icon = Icons.Default.Chat,
        title = "No chats yet",
        description = "Start a new conversation to see your chats here"
    )
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
