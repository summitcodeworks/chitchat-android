package com.summitcodeworks.chitchat.domain.model

data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val isRegistered: Boolean = false,
    val registeredUser: User? = null
)