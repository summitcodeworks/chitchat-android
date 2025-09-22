package com.summitcodeworks.chitchat.domain.usecase.user

import android.content.Context
import android.net.Uri
import com.summitcodeworks.chitchat.data.repository.AuthRepository
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.domain.usecase.media.UploadMediaUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadMediaUseCase: UploadMediaUseCase,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        name: String,
        bio: String? = null,
        avatarUri: Uri? = null
    ): Result<User> {
        return try {
            val token = authRepository.getCurrentUserToken() ?: return Result.failure(Exception("User not authenticated"))

            var avatarUrl: String? = null

            if (avatarUri != null) {
                val avatarFile = createFileFromUri(avatarUri)
                val uploadResult = uploadMediaUseCase(
                    token = token,
                    file = avatarFile,
                    type = "image",
                    description = "Profile picture"
                )

                uploadResult.fold(
                    onSuccess = { media ->
                        avatarUrl = media.url
                    },
                    onFailure = { exception ->
                        return Result.failure(exception)
                    }
                )
            }

            val result = authRepository.updateUserProfile(token, name, avatarUrl, bio)
            result.fold(
                onSuccess = { userDto ->
                    val user = User(
                        id = userDto.id,
                        phoneNumber = userDto.phoneNumber,
                        name = userDto.name,
                        avatarUrl = userDto.avatarUrl,
                        about = userDto.about,
                        lastSeen = userDto.lastSeen,
                        isOnline = userDto.isOnline,
                        createdAt = userDto.createdAt
                    )
                    Result.success(user)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createFileFromUri(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}
