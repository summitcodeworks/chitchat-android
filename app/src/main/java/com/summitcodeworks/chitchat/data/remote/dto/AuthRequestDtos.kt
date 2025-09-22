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
