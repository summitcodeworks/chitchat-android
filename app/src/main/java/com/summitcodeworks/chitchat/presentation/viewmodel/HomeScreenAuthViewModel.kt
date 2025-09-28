package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.summitcodeworks.chitchat.data.auth.OtpAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Simple ViewModel to provide OtpAuthManager to HomeScreen
 */
@HiltViewModel
class HomeScreenAuthViewModel @Inject constructor(
    val otpAuthManager: OtpAuthManager
) : ViewModel()