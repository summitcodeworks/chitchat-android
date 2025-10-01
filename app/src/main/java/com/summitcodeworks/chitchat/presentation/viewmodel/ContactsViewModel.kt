package com.summitcodeworks.chitchat.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.summitcodeworks.chitchat.domain.model.Contact
import com.summitcodeworks.chitchat.domain.model.User
import com.summitcodeworks.chitchat.domain.usecase.user.CheckMultiplePhoneNumbersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkMultiplePhoneNumbersUseCase: CheckMultiplePhoneNumbersUseCase
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    init {
        checkPermission()
    }

    fun checkPermission() {
        val hasContactPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        _hasPermission.value = hasContactPermission
        if (hasContactPermission) {
            loadContacts()
        }
    }

    fun onPermissionGranted() {
        _hasPermission.value = true
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val contactsList = withContext(Dispatchers.IO) {
                    getDeviceContacts()
                }
                
                // Check which contacts are registered on ChitChat
                val enrichedContacts = checkRegisteredContacts(contactsList)
                
                _contacts.value = enrichedContacts.sortedBy { it.name }
            } catch (e: Exception) {
                // Handle error - could emit error state
                _contacts.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkRegisteredContacts(contacts: List<Contact>): List<Contact> {
        if (contacts.isEmpty()) return contacts

        // Extract phone numbers and generate all possible variations for each
        val phoneNumberVariations = contacts.flatMap { contact ->
            generatePhoneNumberVariations(contact.phone)
        }.distinct()

        return try {
            val result = checkMultiplePhoneNumbersUseCase(phoneNumberVariations)
            result.fold(
                onSuccess = { checkResponse ->
                    // Create a map of all phone number variations to registration info
                    val registrationMap = mutableMapOf<String, com.summitcodeworks.chitchat.data.remote.dto.PhoneCheckResult>()
                    
                    checkResponse.results.forEach { result ->
                        // Store with the exact returned number and all its variations
                        val variations = generatePhoneNumberVariations(result.phoneNumber)
                        variations.forEach { variation ->
                            if (result.exists) {
                                registrationMap[variation] = result
                            }
                        }
                    }

                    // Enrich contacts with registration status
                    contacts.map { contact ->
                        // Try to find a match using any variation of the contact's phone number
                        val contactVariations = generatePhoneNumberVariations(contact.phone)
                        val checkResult = contactVariations.firstNotNullOfOrNull { 
                            registrationMap[it] 
                        }
                        
                        if (checkResult?.exists == true && checkResult.user != null) {
                            contact.copy(
                                isRegistered = true,
                                registeredUser = User(
                                    id = checkResult.user.id,
                                    phoneNumber = checkResult.user.phoneNumber,
                                    name = checkResult.user.name,
                                    avatarUrl = checkResult.user.avatarUrl,
                                    about = checkResult.user.about,
                                    lastSeen = checkResult.user.lastSeen,
                                    isOnline = checkResult.user.isOnline,
                                    createdAt = checkResult.user.createdAt
                                )
                            )
                        } else {
                            contact.copy(isRegistered = false, registeredUser = null)
                        }
                    }
                },
                onFailure = {
                    // If API fails, return contacts without registration info
                    contacts.map { it.copy(isRegistered = false, registeredUser = null) }
                }
            )
        } catch (e: Exception) {
            // If API fails, return contacts without registration info
            contacts.map { it.copy(isRegistered = false, registeredUser = null) }
        }
    }

    private fun generatePhoneNumberVariations(phoneNumber: String): List<String> {
        // Remove all non-digit characters except +
        val digitsOnly = phoneNumber.replace(Regex("[^\\d+]"), "")
        
        if (digitsOnly.isEmpty()) return emptyList()
        
        val variations = mutableSetOf<String>()
        
        // Get just the digits (no +)
        val pureDigits = digitsOnly.replace("+", "")
        
        // Add the original cleaned number
        variations.add(digitsOnly)
        
        // Try with + prefix if not already there
        if (!digitsOnly.startsWith("+")) {
            variations.add("+$pureDigits")
        }
        
        // Always add the pure digits version
        variations.add(pureDigits)
        
        // Country-specific variations (major markets)
        when {
            // India: +91 (10 digits, starts with 6-9)
            pureDigits.startsWith("91") && pureDigits.length == 12 -> {
                variations.add("+91${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            pureDigits.length == 10 && pureDigits.firstOrNull() in '6'..'9' -> {
                variations.add("+91$pureDigits")
                variations.add("91$pureDigits")
            }
            
            // USA/Canada: +1 (10 digits)
            pureDigits.startsWith("1") && pureDigits.length == 11 -> {
                variations.add("+1${pureDigits.substring(1)}")
                variations.add(pureDigits.substring(1))
            }
            pureDigits.length == 10 && pureDigits.firstOrNull() in '2'..'9' -> {
                variations.add("+1$pureDigits")
                variations.add("1$pureDigits")
            }
            
            // UK: +44 (10 or 9 digits after leading 0)
            pureDigits.startsWith("44") && pureDigits.length >= 12 -> {
                variations.add("+44${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            pureDigits.startsWith("0") && pureDigits.length in 10..11 -> {
                variations.add("+44${pureDigits.substring(1)}")
                variations.add("44${pureDigits.substring(1)}")
            }
            
            // China: +86 (11 digits, starts with 1)
            pureDigits.startsWith("86") && pureDigits.length == 13 -> {
                variations.add("+86${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            pureDigits.length == 11 && pureDigits.startsWith("1") -> {
                variations.add("+86$pureDigits")
                variations.add("86$pureDigits")
            }
            
            // Australia: +61 (9 digits after 0, like 04XXXXXXXX)
            pureDigits.startsWith("61") && pureDigits.length == 11 -> {
                variations.add("+61${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            pureDigits.startsWith("0") && pureDigits.length == 10 && pureDigits[1] == '4' -> {
                variations.add("+61${pureDigits.substring(1)}")
                variations.add("61${pureDigits.substring(1)}")
            }
            
            // Germany: +49 (10-11 digits, mobiles start with 1)
            pureDigits.startsWith("49") && pureDigits.length >= 12 -> {
                variations.add("+49${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Brazil: +55 (11 digits)
            pureDigits.startsWith("55") && pureDigits.length == 13 -> {
                variations.add("+55${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            pureDigits.length == 11 -> {
                variations.add("+55$pureDigits")
                variations.add("55$pureDigits")
            }
            
            // Japan: +81 (10-11 digits)
            pureDigits.startsWith("81") && pureDigits.length >= 12 -> {
                variations.add("+81${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Russia: +7 (10 digits, mobiles often 9XXXXXXXXX)
            pureDigits.startsWith("7") && pureDigits.length == 11 -> {
                variations.add("+7${pureDigits.substring(1)}")
                variations.add(pureDigits.substring(1))
            }
            pureDigits.length == 10 && pureDigits.startsWith("9") -> {
                variations.add("+7$pureDigits")
                variations.add("7$pureDigits")
            }
            
            // France: +33 (9 digits after 0, like 06XXXXXXXX)
            pureDigits.startsWith("33") && pureDigits.length == 11 -> {
                variations.add("+33${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            pureDigits.startsWith("0") && pureDigits.length == 10 && pureDigits[1] in '6'..'7' -> {
                variations.add("+33${pureDigits.substring(1)}")
                variations.add("33${pureDigits.substring(1)}")
            }
            
            // South Africa: +27 (9 digits after 0)
            pureDigits.startsWith("27") && pureDigits.length == 11 -> {
                variations.add("+27${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            pureDigits.startsWith("0") && pureDigits.length == 10 && pureDigits[1] in '6'..'8' -> {
                variations.add("+27${pureDigits.substring(1)}")
                variations.add("27${pureDigits.substring(1)}")
            }
            
            // Mexico: +52 (10 digits)
            pureDigits.startsWith("52") && pureDigits.length == 12 -> {
                variations.add("+52${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            
            // Indonesia: +62 (10-12 digits)
            pureDigits.startsWith("62") && pureDigits.length >= 12 -> {
                variations.add("+62${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Pakistan: +92 (10 digits)
            pureDigits.startsWith("92") && pureDigits.length == 12 -> {
                variations.add("+92${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Bangladesh: +880 (10 digits after 0)
            pureDigits.startsWith("880") && pureDigits.length == 13 -> {
                variations.add("+880${pureDigits.substring(3)}")
                variations.add("0${pureDigits.substring(3)}")
            }
            pureDigits.startsWith("0") && pureDigits.length == 11 && pureDigits[1] == '1' -> {
                variations.add("+880${pureDigits.substring(1)}")
                variations.add("880${pureDigits.substring(1)}")
            }
            
            // Nepal: +977 (10 digits, starts with 98/97)
            pureDigits.startsWith("977") && pureDigits.length == 13 -> {
                variations.add("+977${pureDigits.substring(3)}")
                variations.add(pureDigits.substring(3))
            }
            pureDigits.length == 10 && pureDigits.startsWith("9") -> {
                variations.add("+977$pureDigits")
                variations.add("977$pureDigits")
            }
            
            // UAE: +971 (9 digits, 05XXXXXXXX)
            pureDigits.startsWith("971") && pureDigits.length == 12 -> {
                variations.add("+971${pureDigits.substring(3)}")
                variations.add("0${pureDigits.substring(3)}")
            }
            pureDigits.startsWith("0") && pureDigits.length == 10 && pureDigits[1] == '5' -> {
                variations.add("+971${pureDigits.substring(1)}")
                variations.add("971${pureDigits.substring(1)}")
            }
            
            // Saudi Arabia: +966 (9 digits, 05XXXXXXXX)
            pureDigits.startsWith("966") && pureDigits.length == 12 -> {
                variations.add("+966${pureDigits.substring(3)}")
                variations.add("0${pureDigits.substring(3)}")
            }
            
            // Turkey: +90 (10 digits, starts with 5)
            pureDigits.startsWith("90") && pureDigits.length == 12 -> {
                variations.add("+90${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            
            // Italy: +39 (9-10 digits, mobiles start with 3)
            pureDigits.startsWith("39") && pureDigits.length >= 11 -> {
                variations.add("+39${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            
            // Spain: +34 (9 digits, starts with 6 or 7)
            pureDigits.startsWith("34") && pureDigits.length == 11 -> {
                variations.add("+34${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            pureDigits.length == 9 && pureDigits.firstOrNull() in '6'..'7' -> {
                variations.add("+34$pureDigits")
                variations.add("34$pureDigits")
            }
            
            // Nigeria: +234 (10 digits, starts with 7/8/9)
            pureDigits.startsWith("234") && pureDigits.length == 13 -> {
                variations.add("+234${pureDigits.substring(3)}")
                variations.add("0${pureDigits.substring(3)}")
            }
            
            // Singapore: +65 (8 digits, starts with 8 or 9)
            pureDigits.startsWith("65") && pureDigits.length == 10 -> {
                variations.add("+65${pureDigits.substring(2)}")
                variations.add(pureDigits.substring(2))
            }
            pureDigits.length == 8 && pureDigits.firstOrNull() in '8'..'9' -> {
                variations.add("+65$pureDigits")
                variations.add("65$pureDigits")
            }
            
            // Philippines: +63 (10 digits, starts with 9)
            pureDigits.startsWith("63") && pureDigits.length == 12 -> {
                variations.add("+63${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Thailand: +66 (9 digits, 08/09XXXXXXXX)
            pureDigits.startsWith("66") && pureDigits.length == 11 -> {
                variations.add("+66${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Vietnam: +84 (9-10 digits)
            pureDigits.startsWith("84") && pureDigits.length >= 11 -> {
                variations.add("+84${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // South Korea: +82 (9-10 digits, 010XXXXXXXX)
            pureDigits.startsWith("82") && pureDigits.length >= 11 -> {
                variations.add("+82${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
            
            // Malaysia: +60 (9-10 digits)
            pureDigits.startsWith("60") && pureDigits.length >= 11 -> {
                variations.add("+60${pureDigits.substring(2)}")
                variations.add("0${pureDigits.substring(2)}")
            }
        }
        
        return variations.toList()
    }

    private fun isValidMobileNumber(phoneNumber: String): Boolean {
        // Remove all non-digit characters except +
        val cleaned = phoneNumber.replace(Regex("[^\\d+]"), "")
        val digitsOnly = cleaned.replace("+", "")
        
        // Filter out invalid numbers
        if (digitsOnly.isEmpty()) return false
        
        // Length checks (E.164 standard: max 15 digits)
        val length = digitsOnly.length
        if (length < 7 || length > 15) return false
        
        // Exclude short codes (typically 3-6 digits)
        if (length <= 6) return false
        
        // Exclude emergency and service numbers
        val serviceNumbers = setOf(
            "911", "112", "999", "100", "101", "102", "108", // Emergency
            "411", "311", "511", "211", "811", // Information services
            "1800", "1888", "1877", "1866", "1855", "1844", "1833", "1822", // Toll-free (North America)
            "0800", "0808", // Toll-free (UK, others)
            "1300", "1800", // Australia toll-free
            "0000", "1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999" // Test numbers
        )
        
        // Check if number starts with any service number pattern
        if (serviceNumbers.any { digitsOnly.startsWith(it) && digitsOnly.length <= it.length + 1 }) {
            return false
        }
        
        // Exclude numbers with all same digits (like 111111, 999999)
        if (digitsOnly.all { it == digitsOnly[0] }) return false
        
        // Exclude sequential numbers (like 123456, 987654)
        if (isSequentialNumber(digitsOnly)) return false
        
        return true
    }
    
    private fun isSequentialNumber(number: String): Boolean {
        if (number.length < 6) return false
        
        var ascending = 0
        var descending = 0
        
        for (i in 0 until number.length - 1) {
            val current = number[i].digitToIntOrNull() ?: return false
            val next = number[i + 1].digitToIntOrNull() ?: return false
            
            if (next == current + 1) ascending++
            if (next == current - 1) descending++
        }
        
        // If more than 5 consecutive ascending or descending digits
        return ascending >= 5 || descending >= 5
    }

    private fun getDeviceContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn) ?: "Unknown"
                val phoneNumber = it.getString(numberColumn) ?: ""

                // Filter out service numbers and invalid mobile numbers
                if (!isValidMobileNumber(phoneNumber)) {
                    continue
                }

                // Avoid duplicates - only add if we don't already have this contact
                if (contacts.none { contact -> contact.id == id }) {
                    contacts.add(
                        Contact(
                            id = id,
                            name = name,
                            phone = phoneNumber
                        )
                    )
                }
            }
        }

        return contacts
    }
}