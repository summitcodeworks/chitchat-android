package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.remote.dto.UserDto
import com.summitcodeworks.chitchat.data.repository.OtpAuthRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository
) {
    suspend operator fun invoke(userId: Long): Result<UserDto> {
        return otpAuthRepository.getUserById(userId)
    }
}
