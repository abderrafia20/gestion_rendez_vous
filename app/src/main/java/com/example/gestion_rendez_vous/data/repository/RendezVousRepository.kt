package com.example.gestion_rendez_vous.data.repository

import com.example.gestion_rendez_vous.data.model.RendezVous
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining medical appointment CRUD operations.
 */
interface RendezVousRepository {
    fun getRendezVousFlow(userId: String): Flow<List<RendezVous>>
    suspend fun getRendezVousById(id: String): Result<RendezVous?>
    suspend fun createRendezVous(rendezVous: RendezVous): Result<String>
    suspend fun updateRendezVous(rendezVous: RendezVous): Result<Unit>
    suspend fun deleteRendezVous(id: String): Result<Unit>
}
