package com.example.gestion_rendez_vous.data.repository

import com.example.gestion_rendez_vous.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AuthRepository] using Firebase Auth and Firebase Firestore.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, mdp: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, mdp).await()
            val firebaseUser = authResult.user ?: throw Exception("Utilisateur introuvable après connexion.")
            
            // Retrieve user profile data from Firestore
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = doc.toObject(User::class.java) ?: User(
                uid = firebaseUser.uid,
                nom = firebaseUser.displayName ?: "Utilisateur",
                email = email
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(nom: String, email: String, mdp: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, mdp).await()
            val firebaseUser = authResult.user ?: throw Exception("La création du compte a échoué.")
            
            val newUser = User(uid = firebaseUser.uid, nom = nom, email = email)
            
            // Store registration info in Firestore
            firestore.collection("users").document(newUser.uid).set(newUser).await()
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            nom = firebaseUser.displayName ?: "Utilisateur",
            email = firebaseUser.email ?: ""
        )
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun observeSessionState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                trySend(User(
                    uid = firebaseUser.uid,
                    nom = firebaseUser.displayName ?: "Utilisateur",
                    email = firebaseUser.email ?: ""
                ))
            } else {
                trySend(null)
            }
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }
}
