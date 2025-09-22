package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.repository.AuthRepository
import com.summitcodeworks.chitchat.domain.model.User
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return try {
            val token = authRepository.getCurrentUserToken() ?: return Result.failure(Exception("User not authenticated"))
            val result = authRepository.getUserProfile(token)
            result.fold(
                onSuccess = { userDto ->
                    val user = User(
                        id = userDto.id,
                        phoneNumber = userDto.phoneNumber,
                        name = userDto.name,
                        avatarUrl = userDto.avatarUrl,
                        about = userDto.about,
                        lastSeen = userDto.lastSeen,
                        isOnline = userDto.isOnline,
                        createdAt = userDto.createdAt
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
