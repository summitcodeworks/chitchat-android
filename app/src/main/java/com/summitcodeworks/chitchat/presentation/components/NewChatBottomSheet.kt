package com.summitcodeworks.chitchat.presentation.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.chitchat.presentation.viewmodel.ContactsViewModel
import com.summitcodeworks.chitchat.domain.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatBottomSheet(
    onDismiss: () -> Unit,
    onNewGroupChat: () -> Unit = {},
    onNewBroadcast: () -> Unit = {},
    onLinkedDevices: () -> Unit = {},
    onStarredMessages: () -> Unit = {},
    onSettings: () -> Unit = {},
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier,
    contactsViewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by contactsViewModel.contacts.collectAsStateWithLifecycle()
    val isLoading by contactsViewModel.isLoading.collectAsStateWithLifecycle()
    val hasPermission by contactsViewModel.hasPermission.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var permissionRequested by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactsViewModel.onPermissionGranted()
        }
    }

    // Auto-request permission when bottom sheet opens if not granted
    LaunchedEffect(hasPermission) {
        if (!hasPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // Request focus and show keyboard when search is activated
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            searchFocusRequester.requestFocus()
        }
    }
    
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phone.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header with close button or search bar
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search contacts...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Close search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "New chat",
                            fontWeight = FontWeight.Medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isSearchActive = true
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = {
                            // TODO: Open more options
                        }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Only show quick action items when not searching
                if (!isSearchActive) {
                    // Quick action items
                    item {
                        QuickActionItem(
                            icon = Icons.Default.Group,
                            title = "New group",
                            onClick = {
                                onDismiss()
                                onNewGroupChat()
                            }
                        )
                    }

                    item {
                        QuickActionItem(
                            icon = Icons.Default.Campaign,
                            title = "New broadcast",
                            onClick = {
                                onDismiss()
                                onNewBroadcast()
                            }
                        )
                    }

                    item {
                        QuickActionItem(
                            icon = Icons.Default.Devices,
                            title = "Linked devices",
                            onClick = {
                                onDismiss()
                                onLinkedDevices()
                            }
                        )
                    }

                    item {
                        QuickActionItem(
                            icon = Icons.Default.Star,
                            title = "Starred messages",
                            onClick = {
                                onDismiss()
                                onStarredMessages()
                            }
                        )
                    }

                    item {
                        QuickActionItem(
                            icon = Icons.Default.Settings,
                            title = "Settings",
                            onClick = {
                                onDismiss()
                                onSettings()
                            }
                        )
                    }

                    // Divider
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        )
                    }

                    // Contacts section header
                    item {
                        Text(
                            text = "Contacts",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Show search results count when searching
                    item {
                        Text(
                            text = if (filteredContacts.isEmpty() && searchQuery.isNotEmpty()) {
                                "No contacts found"
                            } else {
                                "${filteredContacts.size} contact${if (filteredContacts.size != 1) "s" else ""} found"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Contact list
                if (!hasPermission) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.ContactPhone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Contacts permission required",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Please grant contacts permission to see your contacts here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredContacts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isSearchActive && searchQuery.isNotEmpty()) {
                                    "No contacts match \"$searchQuery\""
                                } else {
                                    "No contacts found"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredContacts) { contact ->
                        ContactItemRowWithAction(
                            contact = contact,
                            onChatClick = {
                                onDismiss()
                                onContactSelected(contact)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ContactItemRow(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContactItemRowWithAction(
    contact: Contact,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Local contact name from phone
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            // Registered user name (like WhatsApp shows "~Name")
            if (contact.isRegistered && contact.registeredUser != null) {
                contact.registeredUser?.name?.let { registeredName ->
                    if (registeredName != contact.name) {
                        Text(
                            text = "~$registeredName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Phone number
            Text(
                text = contact.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Chat or Invite button
        if (contact.isRegistered) {
            // Chat button
            FilledTonalButton(
                onClick = onChatClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Chat")
            }
        } else {
            // Invite button
            OutlinedButton(
                onClick = {
                    openSmsToInvite(context, contact.phone)
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Invite",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Invite")
            }
        }
    }
}

private fun openSmsToInvite(context: Context, phoneNumber: String) {
    val message = "Hey! Join me on ChitChat, a great messaging app. Download it now!"
    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse("sms:$phoneNumber")
        putExtra("sms_body", message)
    }
    try {
        context.startActivity(smsIntent)
    } catch (e: Exception) {
        // Handle error - could show a toast
        e.printStackTrace()
    }
}

