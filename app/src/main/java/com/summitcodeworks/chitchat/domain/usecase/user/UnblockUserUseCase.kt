package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.repository.AuthRepository
import javax.inject.Inject

class UnblockUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String, userId: Long): Result<Unit> {
        return authRepository.unblockUser(token, userId)
    }
}
