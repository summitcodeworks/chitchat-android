package com.summitcodeworks.chitchat.presentation.screen.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.summitcodeworks.chitchat.data.model.Countries
import com.summitcodeworks.chitchat.data.model.Country
import com.summitcodeworks.chitchat.presentation.components.CountryPickerBottomSheet
import com.summitcodeworks.chitchat.presentation.viewmodel.OtpAuthViewModel

/**
 * OTP Authentication Screen
 * 
 * Handles SMS-based OTP authentication flow:
 * 1. User enters phone number
 * 2. System sends OTP via SMS
 * 3. User enters OTP code
 * 4. System verifies OTP and authenticates user
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpAuthScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OtpAuthViewModel = hiltViewModel()
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(Countries.getDefaultCountry()) }
    var showCountryPicker by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val authResponse by viewModel.authResponse.collectAsState()

    // Update local state when ViewModel state changes
    LaunchedEffect(otpSent) {
        isOtpSent = otpSent
    }

    // Navigate to main app when authentication is successful
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ChitChat Authentication",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        if (!isOtpSent) {
            // Country picker and phone number input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Country picker button - custom styled like OutlinedTextField
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(56.dp)
                        .clickable { showCountryPicker = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Country",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Phone number input
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("1234567890") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumber"
                        viewModel.sendOtp(fullPhoneNumber)
                    }
                },
                enabled = phoneNumber.isNotBlank() && !authState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Send OTP")
                }
            }
        } else {
            // OTP input
            Text(
                text = "Enter the OTP sent to ${selectedCountry.dialCode}$phoneNumber",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("OTP Code") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (otpCode.isNotBlank()) {
                            val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumber"
                            viewModel.verifyOtp(fullPhoneNumber, otpCode)
                        }
                    },
                    enabled = otpCode.isNotBlank() && !authState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Verify OTP")
                    }
                }
                
                Button(
                    onClick = {
                        val fullPhoneNumber = "${selectedCountry.dialCode}$phoneNumber"
                        viewModel.resendOtp(fullPhoneNumber)
                    },
                    enabled = !authState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resend")
                }
            }
        }

        // Error message
        authState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Success message
        authResponse?.let { response ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Authentication Successful!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Welcome, ${response.user.name}!",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
    
    // Country Picker Bottom Sheet
    if (showCountryPicker) {
        CountryPickerBottomSheet(
            onCountrySelected = { country ->
                selectedCountry = country
            },
            onDismiss = { showCountryPicker = false },
            selectedCountry = selectedCountry
        )
    }
}
