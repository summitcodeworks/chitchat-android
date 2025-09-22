package com.summitcodeworks.chitchat.data.config

enum class Environment(
    val displayName: String,
    val apiBaseUrl: String,
    val webSocketBaseUrl: String
) {
    LOCAL(
        displayName = "Local",
        apiBaseUrl = "http://192.168.0.152:9101/",
        webSocketBaseUrl = "ws://192.168.0.152:9101/"
    ),
    PRODUCTION(
        displayName = "Production",
        apiBaseUrl = "http://65.1.185.194:9101/",
        webSocketBaseUrl = "ws://65.1.185.194:9101/"
    );

    companion object {
        fun fromDisplayName(displayName: String): Environment {
            return values().find { it.displayName == displayName } ?: LOCAL
        }
    }
}