package com.summitcodeworks.chitchat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Message
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.domain.model.Conversation
import com.summitcodeworks.chitchat.domain.usecase.message.SearchMessagesUseCase
import com.summitcodeworks.chitchat.domain.usecase.user.CheckMultiplePhoneNumbersUseCase
import com.summitcodeworks.chitchat.data.local.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * ViewModel for handling search functionality in ChitChat.
 * 
 * This ViewModel supports two types of searches:
 * 1. Message Search: Searches through message content when user types regular text
 * 2. Phone Number Search: Searches for users by phone number when user enters a full international phone number
 * 
 * The ViewModel intelligently determines which type of search to perform based on the input:
 * - If input starts with '+' and has 10-15 digits -> Phone number search
 * - If input starts with '+' but has less than 10 digits -> No search (incomplete number)
 * - Otherwise -> Message content search
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMessagesUseCase: SearchMessagesUseCase,
    private val checkMultiplePhoneNumbersUseCase: CheckMultiplePhoneNumbersUseCase,
    private val userDao: UserDao
) : ViewModel() {
    
    // Holds the current search results (messages, users, conversations, or error)
    private val _searchResults = MutableStateFlow<SearchResults>(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults.asStateFlow()
    
    // Indicates whether a search operation is currently in progress
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    // Reference to the current search job, used for cancellation when a new search is triggered
    private var searchJob: Job? = null
    
    /**
     * Main search function that handles user input and determines the appropriate search type.
     * 
     * Flow:
     * 1. Cancel any previous search job to avoid duplicate API calls
     * 2. Return early if query is blank
     * 3. Apply 300ms debounce to avoid excessive API calls while user is typing
     * 4. Check if input is an incomplete phone number (starts with + but less than 10 digits)
     *    - If yes: Don't search at all (prevents wasteful API calls like "api/messages/search?query=%2B91")
     * 5. Check if input is a valid full international phone number (10-15 digits)
     *    - If yes: Search for users by phone number
     *    - If no: Search through message content
     * 
     * @param query The search query entered by the user
     * @param token Authentication token for API calls
     */
    fun search(query: String, token: String?) {
        // Cancel previous search job to prevent duplicate searches
        searchJob?.cancel()
        
        // If query is blank, clear results and return
        if (query.isBlank()) {
            _searchResults.value = SearchResults()
            _isSearching.value = false
            return
        }
        
        _isSearching.value = true
        
        searchJob = viewModelScope.launch {
            // Debounce: Wait 300ms before searching to avoid excessive API calls while user is typing
            delay(300)
            
            val trimmedQuery = query.trim()
            
            // IMPORTANT FIX: Check if query looks like an incomplete phone number
            // Example: "+91" has only 2 digits, so don't search at all
            // This prevents wasteful API calls like "api/messages/search?query=%2B91"
            // where %2B is URL-encoded '+' symbol
            val looksLikeIncompletePhone = trimmedQuery.startsWith("+") && 
                trimmedQuery.replace(Regex("[^\\d]"), "").length < 10
            
            if (looksLikeIncompletePhone) {
                // Don't search for incomplete phone numbers - just wait for user to finish typing
                _searchResults.value = SearchResults()
                _isSearching.value = false
                return@launch
            }
            
            // Check if query is a valid complete international phone number (E.164 format)
            val isValidInternationalPhone = isValidInternationalPhoneNumber(trimmedQuery)
            
            if (isValidInternationalPhone && token != null) {
                // Search for users by phone number
                searchPhoneNumber(trimmedQuery)
            } else if (!isValidInternationalPhone && token != null) {
                // Search through message content
                searchMessages(trimmedQuery, token)
            }
            
            _isSearching.value = false
        }
    }
    
    /**
     * Searches through message content using the backend API.
     * 
     * This function is called when the user enters regular text (not a phone number).
     * It searches through all message content and groups results by conversation.
     * 
     * @param query The search term to find in message content
     * @param token Authentication token for the API call
     */
    private suspend fun searchMessages(query: String, token: String) {
        searchMessagesUseCase(token, query).fold(
            onSuccess = { messages ->
                // Group messages by conversation for better UX
                _searchResults.value = SearchResults(
                    messages = messages,
                    users = emptyList(),
                    conversations = groupMessagesByConversation(messages)
                )
            },
            onFailure = { 
                _searchResults.value = SearchResults(error = it.message)
            }
        )
    }
    
    /**
     * Searches for users by phone number using the backend API.
     * 
     * This function is called when the user enters a valid international phone number.
     * It checks if the phone number is registered in the system and returns matching users.
     * 
     * Flow:
     * 1. Clean the phone number (remove formatting, ensure proper + prefix)
     * 2. Call API to check if phone number exists
     * 3. Filter results to only include existing users
     * 4. Map DTOs to domain User models
     * 
     * @param phoneNumber The phone number to search for (e.g., "+911234567890")
     */
    private suspend fun searchPhoneNumber(phoneNumber: String) {
        // Clean the phone number: remove spaces, dashes, ensure + prefix
        val cleanedPhone = cleanPhoneNumber(phoneNumber)
        
        // Check if this phone number is registered in the system
        checkMultiplePhoneNumbersUseCase(listOf(cleanedPhone)).fold(
            onSuccess = { response ->
                // Filter to only include users that exist in the system
                val foundUsers = response.results
                    .filter { it.exists }
                    .mapNotNull { it.user }
                    .map { userDto ->
                        // Convert DTO to domain model
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
                
                _searchResults.value = SearchResults(
                    messages = emptyList(),
                    users = foundUsers,
                    conversations = emptyList()
                )
            },
            onFailure = {
                _searchResults.value = SearchResults(error = it.message)
            }
        )
    }
    
    /**
     * Validates if a query string is a valid complete international phone number.
     * 
     * Validation criteria (E.164 format):
     * 1. Must start with '+' sign
     * 2. After '+', must contain only digits (0-9)
     * 3. Must have 10-15 digits total (country code + subscriber number)
     * 
     * Examples:
     * - "+911234567890" -> true (India, 12 digits)
     * - "+12025551234" -> true (USA, 11 digits)
     * - "+91" -> false (only 2 digits, incomplete)
     * - "911234567890" -> false (missing + sign)
     * - "+91 12345 67890" -> true (spaces are removed before validation)
     * 
     * @param query The input string to validate
     * @return true if it's a valid complete international phone number, false otherwise
     */
    private fun isValidInternationalPhoneNumber(query: String): Boolean {
        // Remove spaces, dashes, parentheses for validation
        // Example: "+91 12345-67890" becomes "+911234567890"
        val cleaned = query.replace(Regex("[\\s\\-()]+"), "")
        
        // Must start with '+' sign (E.164 format requirement)
        if (!cleaned.startsWith("+")) {
            return false
        }
        
        // Remove the '+' sign and check if the rest are all digits
        val digitsOnly = cleaned.substring(1)
        if (!digitsOnly.all { it.isDigit() }) {
            return false
        }
        
        // International phone numbers (E.164 format) are typically:
        // - Minimum: Country code (1-3 digits) + minimum 7 digits = 8 digits total
        // - Maximum: 15 digits total (including country code)
        // To ensure it's a FULL number, we'll require at least 10 digits (country code + number)
        // This prevents partial numbers like "+91" (2 digits) from being treated as valid
        val digitCount = digitsOnly.length
        return digitCount in 10..15
    }
    
    /**
     * Cleans and formats a phone number to E.164 international format.
     * 
     * This function:
     * 1. Removes all formatting characters (spaces, dashes, parentheses, dots)
     * 2. Ensures only one '+' sign at the beginning
     * 3. Adds '+' prefix if missing but number starts with a digit
     * 
     * Examples:
     * - "+91 12345-67890" -> "+911234567890"
     * - "+91++12345" -> "+9112345"
     * - "911234567890" -> "+911234567890"
     * - "+1 (202) 555-1234" -> "+12025551234"
     * 
     * @param phone The raw phone number input
     * @return Cleaned phone number in E.164 format
     */
    private fun cleanPhoneNumber(phone: String): String {
        // Remove all formatting characters except '+' and digits
        // This handles spaces, dashes, parentheses, dots, etc.
        var cleaned = phone.replace(Regex("[^+\\d]"), "")
        
        // Ensure only one '+' at the beginning
        if (cleaned.startsWith("+")) {
            // Remove any additional '+' signs after the first one
            // Example: "+91++12345" -> "+9112345"
            cleaned = "+" + cleaned.substring(1).replace("+", "")
        } else if (cleaned.isNotEmpty() && cleaned[0].isDigit()) {
            // If no '+' sign but starts with digit, add '+' at the beginning
            // Example: "911234567890" -> "+911234567890"
            cleaned = "+$cleaned"
        }
        
        return cleaned
    }
    
    /**
     * Groups a list of messages by conversation (user).
     * 
     * This function takes search results (individual messages) and organizes them
     * into conversations, making it easier for users to see which chats contain
     * the search term.
     * 
     * Flow:
     * 1. Group messages by the other user (sender or receiver)
     * 2. For each group, get the latest message
     * 3. Fetch user details from local database
     * 4. Create Conversation objects with user info and latest message
     * 5. Sort by most recent message first
     * 
     * @param messages List of messages from search results
     * @return List of conversations sorted by most recent message
     */
    private suspend fun groupMessagesByConversation(messages: List<Message>): List<Conversation> {
        // Map to group messages by user ID
        val conversationsMap = mutableMapOf<Long, MutableList<Message>>()
        
        // Group each message by the other user (sender or receiver)
        messages.forEach { message ->
            val otherUserId = message.receiverId ?: message.senderId
            conversationsMap.getOrPut(otherUserId) { mutableListOf() }.add(message)
        }
        
        // Convert grouped messages to Conversation objects
        return conversationsMap.map { (userId, msgs) ->
            // Get the most recent message for preview
            val latestMessage = msgs.maxByOrNull { it.timestamp }
            
            // Fetch user details from local database
            val user = userDao.getUserById(userId)
            
            Conversation(
                userId = userId,
                userName = user?.name ?: user?.phoneNumber ?: "$userId",
                userAvatar = user?.avatarUrl,
                lastMessage = latestMessage?.content,
                lastMessageTime = latestMessage?.timestamp,
                unreadCount = 0,
                isOnline = user?.isOnline ?: false
            )
        }.sortedByDescending { it.lastMessageTime }  // Most recent conversations first
    }
    
    /**
     * Clears all search results and cancels any ongoing search operations.
     * 
     * This is called when:
     * - User navigates away from the search screen
     * - User clears the search input
     */
    fun clearSearch() {
        searchJob?.cancel()
        _searchResults.value = SearchResults()
        _isSearching.value = false
    }
}

/**
 * Data class representing the results of a search operation.
 * 
 * This class encapsulates all possible types of search results that can be returned
 * from the search functionality. The search can return different types of results
 * depending on the query type:
 * 
 * - Text search: Returns messages and conversations (grouped messages)
 * - Phone number search: Returns users who match the phone number
 * - Error case: Contains error message if search fails
 * 
 * @param messages List of messages matching the search query
 * @param users List of users found by phone number search
 * @param conversations List of conversations created from message search results
 * @param error Error message if the search operation failed
 */
data class SearchResults(
    val messages: List<Message> = emptyList(),
    val users: List<User> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val error: String? = null
)
