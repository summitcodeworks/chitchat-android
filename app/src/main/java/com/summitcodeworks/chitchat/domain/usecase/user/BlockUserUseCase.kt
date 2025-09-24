package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.repository.AuthRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: Long): Result<Unit> {
        return authRepository.blockUser(userId)
    }
}
