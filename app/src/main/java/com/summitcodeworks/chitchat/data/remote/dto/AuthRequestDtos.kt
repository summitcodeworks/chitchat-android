package com.summitcodeworks.chitchat.data.remote.dto

data class AdminLoginRequest(
    val username: String,
    val password: String
)

data class SignInWithPhoneRequest(
    val phoneNumber: String,
    val countryCode: String
)

data class VerifyOtpRequest(
    val phoneNumber: String,
    val otp: String,
    val verificationId: String
)

// New OTP-based authentication DTOs
data class SendOtpRequest(
    val phoneNumber: String
)

data class VerifyOtpSmsRequest(
    val phoneNumber: String,
    val otp: String
)

data class OtpAuthResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val user: UserDto,
    val message: String
)
