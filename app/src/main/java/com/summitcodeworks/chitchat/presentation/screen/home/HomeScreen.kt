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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: (Long) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val user by homeViewModel.user.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val error by homeViewModel.error.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ChitChat",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        // TODO: Navigate to search screen
                        // Could navigate to a search screen for users, groups, messages
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { 
                        // TODO: Navigate to settings screen
                        // Could navigate to app settings, profile settings, etc.
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // TODO: Start new chat
                    // Could open contact picker or navigate to new chat screen
                }
            ) {
                Icon(Icons.Default.Chat, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Welcome Message
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome back!",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentUser?.name ?: "User",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Tab Row
            TabRow(
                selectedTabIndex = 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Chats", "Status", "Calls").forEachIndexed { index, title ->
                    Tab(
                        selected = index == 0,
                        onClick = { 
                            // TODO: Switch tabs
                            // Could switch between different tabs (Chats, Status, Calls)
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            // Chat List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(getMockChats()) { chat ->
                    ChatItem(
                        chat = chat,
                        onClick = { onNavigateToChat(chat.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    chat: MockChat,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.first().toString(),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Chat Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = chat.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    
                    if (chat.unreadCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MockChat(
    val id: Long,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)

fun getMockChats(): List<MockChat> = listOf(
    MockChat(1, "John Doe", "Hey, how are you?", "2:30 PM", 2),
    MockChat(2, "Jane Smith", "See you tomorrow!", "1:15 PM", 0),
    MockChat(3, "Mike Johnson", "Thanks for the help!", "12:45 PM", 1),
    MockChat(4, "Sarah Wilson", "Can we meet today?", "11:20 AM", 0),
    MockChat(5, "David Brown", "Great work on the project!", "10:30 AM", 3)
)
