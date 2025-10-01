package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.repository.OtpAuthRepository
import com.summitcodeworks.chitchat.data.remote.dto.CheckPhonesResponse
import javax.inject.Inject

class CheckMultiplePhoneNumbersUseCase @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository
) {
    suspend operator fun invoke(phoneNumbers: List<String>): Result<CheckPhonesResponse> {
        return try {
            otpAuthRepository.checkMultiplePhoneNumbers(phoneNumbers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
