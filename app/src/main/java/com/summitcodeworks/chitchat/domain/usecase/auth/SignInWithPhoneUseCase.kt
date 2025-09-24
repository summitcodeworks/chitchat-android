package com.summitcodeworks.chitchat.domain.usecase.auth

import com.summitcodeworks.chitchat.data.repository.AuthRepository
import com.summitcodeworks.chitchat.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SignInWithPhoneUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(
        phoneNumber: String,
        verificationId: String,
        code: String
    ): Result<String> {
        return try {
            val firebaseUserResult = authRepository.signInWithPhoneNumber(phoneNumber, verificationId, code)
            
            firebaseUserResult.fold(
                onSuccess = { firebaseUser ->
                    val idToken = firebaseUser.getIdToken(false).await()
                    
                    val authResult = authRepository.authenticateWithBackend(
                        name = firebaseUser.displayName,
                        deviceInfo = "Android"
                    )
                    
                    authResult.fold(
                        onSuccess = { authResponse ->
                            Result.success(authResponse.token)
                        },
                        onFailure = { exception ->
                            Result.failure(exception)
                        }
                    )
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeCurrentUser(): Flow<User?> {
        // This method needs to convert UserEntity to User domain model
        // Simplified for now - would need proper mapping
        return flowOf(null)
    }

    fun getCurrentFirebaseUser() = authRepository.getCurrentUser()

    suspend fun getCurrentUserToken() = authRepository.getCurrentUserToken()

    suspend fun signOut() = authRepository.signOut()

    fun observeAuthState() = authRepository.observeAuthState()
}
