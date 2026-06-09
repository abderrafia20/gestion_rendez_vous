package com.example.gestion_rendez_vous.data.model

/**
 * Data model for security and activity audit logs.
 * Used to keep track of user actions (success/failure) locally.
 */
data class UserActionLog(
    val timestamp: Long = System.currentTimeMillis(),
    val action: String = "",
    val details: String = "",
    val status: String = ""
)
