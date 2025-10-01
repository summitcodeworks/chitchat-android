package com.summitcodeworks.chitchat.domain.model

/**
 * Domain model representing a user in the ChitChat application.
 * 
 * This data class encapsulates all user-related information including
 * profile details, status information, and relationship data. It serves
 * as the primary user representation throughout the application.
 * 
 * User properties:
 * - Basic identification (ID, phone number, name)
 * - Profile information (avatar, about section)
 * - Status tracking (online status, last seen)
 * - Relationship data (blocked status, contact status)
 * - Account metadata (creation timestamp)
 * 
 * Status management:
 * - Online/offline status for real-time presence
 * - Last seen timestamp for activity tracking
 * - Contact relationship for address book integration
 * - Blocked status for privacy and safety
 * 
 * Profile features:
 * - Avatar image support with URL storage
 * - Customizable about section
 * - Display name management
 * - Phone number as unique identifier
 * 
 * The User model is used across:
 * - Chat interfaces for participant display
 * - Contact lists and address book
 * - Profile screens and user details
 * - Search results and user discovery
 * - Blocking and privacy features
 * 
 * @param id Unique identifier for the user
 * @param phoneNumber User's phone number (primary identifier)
 * @param name Display name for the user
 * @param avatarUrl URL of the user's profile picture
 * @param about User's status/about text
 * @param lastSeen Timestamp of last activity
 * @param isOnline Current online status
 * @param createdAt Account creation timestamp
 * @param isBlocked Whether the user is blocked by current user
 * @param isContact Whether the user is in current user's contacts
 */
data class User(
    val id: Long,
    val phoneNumber: String,
    val name: String,
    val avatarUrl: String? = null,
    val about: String? = null,
    val lastSeen: String? = null,
    val isOnline: Boolean = false,
    val createdAt: String? = null,
    val isBlocked: Boolean = false,
    val isContact: Boolean = false
)
