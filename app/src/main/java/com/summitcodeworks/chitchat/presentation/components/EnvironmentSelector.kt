package com.summitcodeworks.chitchat.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.summitcodeworks.chitchat.data.config.Environment
import com.summitcodeworks.chitchat.presentation.viewmodel.EnvironmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentSelector(
    modifier: Modifier = Modifier,
    viewModel: EnvironmentViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentEnvironment by viewModel.currentEnvironment.collectAsStateWithLifecycle()

    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = currentEnvironment.displayName,
            fontWeight = FontWeight.Medium
        )
    }

    if (showDialog) {
        EnvironmentSelectionDialog(
            currentEnvironment = currentEnvironment,
            onEnvironmentSelected = { environment ->
                viewModel.setEnvironment(environment)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun EnvironmentSelectionDialog(
    currentEnvironment: Environment,
    onEnvironmentSelected: (Environment) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Environment",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Choose the server environment:")
                Spacer(modifier = Modifier.height(16.dp))

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
                                fontWeight = FontWeight.Medium
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
                Text("Close")
            }
        }
    )
}