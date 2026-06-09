package com.example.gestion_rendez_vous.data.repository

import com.example.gestion_rendez_vous.data.model.RendezVous
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [RendezVousRepository] using Firebase Firestore.
 */
@Singleton
class RendezVousRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RendezVousRepository {

    private val rdvCollection = firestore.collection("rendezvous")

    override fun getRendezVousFlow(userId: String): Flow<List<RendezVous>> = callbackFlow {
        // Query only matching user appointments. We do not use orderBy("createdAt") here
        // to avoid Firestore index-creation requirements, preventing crash errors on fresh configurations.
        val subscription = rdvCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(RendezVous::class.java)
                    // Sort locally on client side to keep app running smoothly without dependencies on Firestore indexes.
                    val sortedList = list.sortedByDescending { it.createdAt }
                    trySend(sortedList)
                }
            }
        awaitClose {
            subscription.remove()
        }
    }

    override suspend fun getRendezVousById(id: String): Result<RendezVous?> {
        return try {
            val doc = rdvCollection.document(id).get().await()
            val rdv = doc.toObject(RendezVous::class.java)
            Result.success(rdv)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRendezVous(rendezVous: RendezVous): Result<String> {
        return try {
            val docRef = rdvCollection.document()
            val id = docRef.id
            val finalRdv = rendezVous.copy(id = id)
            docRef.set(finalRdv).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRendezVous(rendezVous: RendezVous): Result<Unit> {
        return try {
            rdvCollection.document(rendezVous.id).set(rendezVous).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRendezVous(id: String): Result<Unit> {
        return try {
            rdvCollection.document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
