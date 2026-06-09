package com.example.gestion_rendez_vous.data.model

/**
 * Data model representing a User profile.
 * Default values are provided to allow Firestore's automatic deserialization.
 */
data class User(
    val uid: String = "",
    val nom: String = "",
    val email: String = ""
)
