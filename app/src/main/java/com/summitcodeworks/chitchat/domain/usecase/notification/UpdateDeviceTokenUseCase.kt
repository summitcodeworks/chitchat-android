package com.summitcodeworks.chitchat.domain.usecase.notification

import com.summitcodeworks.chitchat.data.firebase.FirebaseTokenManager
import com.summitcodeworks.chitchat.data.remote.dto.DeviceTokenUpdateRequest
import com.summitcodeworks.chitchat.data.repository.AuthRepository
import com.summitcodeworks.chitchat.domain.model.User
import javax.inject.Inject

/**
 * Use case for updating device token on the server
 */
class UpdateDeviceTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseTokenManager: FirebaseTokenManager
) {

    /**
     * Updates the device token on the server
     * @param token JWT token for authentication
     * @return Result with updated user data or error
     */
    suspend operator fun invoke(token: String): Result<User> {
        return try {
            // Get Firebase device token
            val deviceTokenResult = firebaseTokenManager.getDeviceToken()

            if (deviceTokenResult.isFailure) {
                return Result.failure(
                    deviceTokenResult.exceptionOrNull() ?: Exception("Failed to get device token")
                )
            }

            val deviceToken = deviceTokenResult.getOrThrow()

            // Create request
            val request = DeviceTokenUpdateRequest(deviceToken = deviceToken)

            // Call API to update device token
            val result = authRepository.updateDeviceToken(token, request)

            result.fold(
                onSuccess = { userDto ->
                    // Convert DTO to domain model
                    val user = User(
                        id = userDto.id,
                        phoneNumber = userDto.phoneNumber,
                        name = userDto.name,
                        avatarUrl = userDto.avatarUrl,
                        about = userDto.about,
                        lastSeen = userDto.lastSeen,
                        isOnline = userDto.isOnline
                    )
                    Result.success(user)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates device token using the current saved token from FirebaseTokenManager
     * This is useful when you don't want to refresh the token
     */
    suspend fun updateWithSavedToken(token: String): Result<User> {
        return try {
            val savedToken = firebaseTokenManager.getSavedToken()

            if (savedToken.isNullOrBlank()) {
                return Result.failure(Exception("No saved device token found"))
            }

            // Create request with saved token
            val request = DeviceTokenUpdateRequest(deviceToken = savedToken)

            // Call API to update device token
            val result = authRepository.updateDeviceToken(token, request)

            result.fold(
                onSuccess = { userDto ->
                    // Convert DTO to domain model
                    val user = User(
                        id = userDto.id,
                        phoneNumber = userDto.phoneNumber,
                        name = userDto.name,
                        avatarUrl = userDto.avatarUrl,
                        about = userDto.about,
                        lastSeen = userDto.lastSeen,
                        isOnline = userDto.isOnline
                    )
                    Result.success(user)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}