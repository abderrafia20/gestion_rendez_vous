package com.example.gestion_rendez_vous.data.repository

import com.example.gestion_rendez_vous.data.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining authentication operations.
 */
interface AuthRepository {
    suspend fun login(email: String, mdp: String): Result<User>
    suspend fun register(nom: String, email: String, mdp: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun resetPassword(email: String): Result<Unit>
    fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean
    fun observeSessionState(): Flow<User?>
}
