package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.repository.AuthRepository
import javax.inject.Inject

class UpdateOnlineStatusUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(isOnline: Boolean): Result<Unit> {
        return authRepository.updateOnlineStatus(isOnline)
    }
}
