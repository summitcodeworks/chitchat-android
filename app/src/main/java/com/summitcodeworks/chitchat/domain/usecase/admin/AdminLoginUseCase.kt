package com.summitcodeworks.chitchat.domain.usecase.admin

import com.summitcodeworks.chitchat.data.remote.api.AdminAuthResponse
import com.summitcodeworks.chitchat.data.repository.AdminRepository
import javax.inject.Inject

class AdminLoginUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(
        username: String,
        password: String
    ): Result<AdminAuthResponse> {
        return adminRepository.adminLogin(username, password)
    }
}
