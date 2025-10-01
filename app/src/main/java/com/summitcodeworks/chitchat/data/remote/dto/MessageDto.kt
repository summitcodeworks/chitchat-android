package com.summitcodeworks.chitchat.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import com.google.gson.*
import java.lang.reflect.Type

data class MessageDto(
    val id: String,
    val senderId: Long,
    @SerializedName("recipientId")
    val receiverId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    @SerializedName("type")
    val messageType: String, // TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT
    @SerializedName("createdAt")
    val timestamp: String? = null,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val replyToMessageId: String? = null,
    val mediaId: Long? = null,
    val isDeleted: Boolean = false,
    val deleteForEveryone: Boolean = false,
    val sender: UserDto? = null,
    val media: MediaDto? = null
)

data class SendMessageRequest(
    val recipientId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    val type: String = "TEXT",
    val replyToMessageId: String? = null,
    val mediaId: Long? = null
)

data class MessagePageResponse(
    val content: List<MessageDto>,
    val pageable: Pageable,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)

data class Pageable(
    @JsonAdapter(SortDeserializer::class)
    val sort: Sort?,
    val offset: Int,
    val pageSize: Int,
    val pageNumber: Int,
    val paged: Boolean,
    val unpaged: Boolean
)

data class Sort(
    val sorted: Boolean? = null,
    val unsorted: Boolean? = null,
    val empty: Boolean? = null
)

// Custom deserializer to handle both array and object formats for sort field
class SortDeserializer : JsonDeserializer<Sort?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Sort? {
        return try {
            when {
                json == null || json.isJsonNull -> null
                json.isJsonArray -> null // If it's an array, return null
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    Sort(
                        sorted = obj.get("sorted")?.asBoolean,
                        unsorted = obj.get("unsorted")?.asBoolean,
                        empty = obj.get("empty")?.asBoolean
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null // Return null on any parsing error
        }
    }
}

data class DeleteMessageRequest(
    val deleteForEveryone: Boolean = false
)

data class MarkMessageReadRequest(
    val messageId: String
)
