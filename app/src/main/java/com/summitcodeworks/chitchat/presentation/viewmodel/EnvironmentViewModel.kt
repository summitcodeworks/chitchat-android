package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.summitcodeworks.chitchat.data.config.Environment
import com.summitcodeworks.chitchat.data.config.EnvironmentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EnvironmentViewModel @Inject constructor(
    private val environmentManager: EnvironmentManager
) : ViewModel() {

    val currentEnvironment: StateFlow<Environment> = environmentManager.currentEnvironment

    fun setEnvironment(environment: Environment) {
        environmentManager.setEnvironment(environment)
    }

    fun getAllEnvironments(): List<Environment> {
        return environmentManager.getAllEnvironments()
    }
}