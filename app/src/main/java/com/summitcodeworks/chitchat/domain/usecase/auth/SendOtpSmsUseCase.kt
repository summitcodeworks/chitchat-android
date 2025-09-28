package com.summitcodeworks.chitchat.domain.usecase.auth

import com.summitcodeworks.chitchat.data.repository.OtpAuthRepository
import javax.inject.Inject

/**
 * Use case for sending OTP via SMS
 */
class SendOtpSmsUseCase @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository
) {
    suspend operator fun invoke(phoneNumber: String): Result<Unit> {
        return otpAuthRepository.sendOtp(phoneNumber)
    }
}
