package com.example.gestion_rendez_vous.data.model

/**
 * Data model representing a medical appointment.
 * Default values are provided to allow Firestore's automatic deserialization.
 */
data class RendezVous(
    val id: String = "",
    val userId: String = "",
    val nomPatient: String = "",
    val nomMedecin: String = "",
    val specialite: String = "",
    val date: String = "",        // Format: dd/MM/yyyy
    val heure: String = "",       // Format: HH:mm
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
