package com.summitcodeworks.chitchat.domain.model

/**
 * Domain model representing a chat message in ChitChat.
 * 
 * This data class encapsulates all information related to a chat message,
 * including content, metadata, delivery status, and associated media.
 * It serves as the primary data structure for message handling throughout
 * the application.
 * 
 * Message properties:
 * - Unique identifier and sender/receiver information
 * - Message content and type classification
 * - Timestamp and delivery status tracking
 * - Media attachments and file references
 * - Reply threading and message deletion flags
 * - User information for display purposes
 * 
 * Message delivery states:
 * - Sending: Message is being transmitted
 * - Delivered: Message reached the server
 * - Read: Message was viewed by recipient
 * 
 * Support for different conversation types:
 * - Direct messages (receiverId specified)
 * - Group messages (groupId specified)
 * - Broadcast messages (multiple recipients)
 * 
 * @param id Unique identifier for the message
 * @param senderId ID of the user who sent the message
 * @param receiverId ID of the direct message recipient (null for group messages)
 * @param groupId ID of the group for group messages (null for direct messages)
 * @param content The actual message content (text or description)
 * @param messageType Type of message content (TEXT, IMAGE, VIDEO, etc.)
 * @param timestamp ISO timestamp when the message was created
 * @param isRead Whether the message has been read by the recipient
 * @param isDelivered Whether the message has been delivered to the server
 * @param replyToMessageId ID of the message being replied to (for threading)
 * @param mediaId ID of associated media file (for media messages)
 * @param isDeleted Whether the message has been deleted locally
 * @param deleteForEveryone Whether the message was deleted for all recipients
 * @param sender User information of the message sender (for display)
 * @param media Media object for attached files (images, videos, etc.)
 */
data class Message(
    val id: String,
    val senderId: Long,
    val receiverId: Long? = null,
    val groupId: Long? = null,
    val content: String,
    val messageType: MessageType,
    val timestamp: String,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val replyToMessageId: String? = null,
    val mediaId: Long? = null,
    val isDeleted: Boolean = false,
    val deleteForEveryone: Boolean = false,
    val sender: User? = null,
    val media: Media? = null
)

/**
 * Enumeration of supported message types in ChitChat.
 * 
 * This enum defines the different types of content that can be sent
 * as messages in the application. Each type has specific handling
 * requirements for display, storage, and transmission.
 * 
 * Supported message types:
 * - TEXT: Plain text messages with formatting support
 * - IMAGE: Image files with thumbnail generation and compression
 * - VIDEO: Video files with preview generation and metadata
 * - AUDIO: Audio recordings and voice notes with waveform data
 * - DOCUMENT: File attachments with type detection and icons
 * 
 * Each message type requires:
 * - Appropriate UI rendering components
 * - Media processing and optimization
 * - Storage and bandwidth considerations
 * - Preview generation for better UX
 * - Accessibility features and descriptions
 */
enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT
}
