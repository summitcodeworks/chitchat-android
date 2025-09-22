package com.summitcodeworks.chitchat.domain.usecase.user

import com.summitcodeworks.chitchat.data.repository.AuthRepository
import com.summitcodeworks.chitchat.domain.model.User
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        token: String,
        contacts: List<Contact>
    ): Result<List<User>> {
        return try {
            val contactDtos = contacts.map { contact ->
                com.summitcodeworks.chitchat.data.remote.dto.ContactDto(
                    phoneNumber = contact.phoneNumber,
                    displayName = contact.displayName
                )
            }
            
            val result = authRepository.syncContacts(token, contactDtos)
            result.fold(
                onSuccess = { userDtos ->
                    val users = userDtos.map { userDto ->
                        User(
                            id = userDto.id,
                            phoneNumber = userDto.phoneNumber,
                            name = userDto.name,
                            avatarUrl = userDto.avatarUrl,
                            about = userDto.about,
                            lastSeen = userDto.lastSeen,
                            isOnline = userDto.isOnline,
                            createdAt = userDto.createdAt
                        )
                    }
                    Result.success(users)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class Contact(
    val phoneNumber: String,
    val displayName: String
)
