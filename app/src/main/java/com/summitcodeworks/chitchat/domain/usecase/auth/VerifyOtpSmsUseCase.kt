package com.summitcodeworks.chitchat.domain.usecase.auth

import com.summitcodeworks.chitchat.data.repository.OtpAuthRepository
import com.summitcodeworks.chitchat.data.remote.dto.OtpAuthResponse
import javax.inject.Inject

/**
 * Use case for verifying OTP and authenticating user
 */
class VerifyOtpSmsUseCase @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, otp: String): Result<OtpAuthResponse> {
        return otpAuthRepository.verifyOtp(phoneNumber, otp)
    }
}
