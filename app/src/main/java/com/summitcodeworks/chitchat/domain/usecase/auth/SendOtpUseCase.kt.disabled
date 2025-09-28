package com.summitcodeworks.chitchat.domain.usecase.auth

import android.app.Activity
import com.google.firebase.auth.PhoneAuthProvider
import com.summitcodeworks.chitchat.data.repository.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    operator fun invoke(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        authRepository.sendOtp(phoneNumber, activity, callbacks)
    }
}