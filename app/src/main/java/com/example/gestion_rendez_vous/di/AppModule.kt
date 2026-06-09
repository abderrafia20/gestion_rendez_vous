package com.example.gestion_rendez_vous.di

import com.example.gestion_rendez_vous.data.repository.AuthRepository
import com.example.gestion_rendez_vous.data.repository.AuthRepositoryImpl
import com.example.gestion_rendez_vous.data.repository.LogRepository
import com.example.gestion_rendez_vous.data.repository.LogRepositoryImpl
import com.example.gestion_rendez_vous.data.repository.RendezVousRepository
import com.example.gestion_rendez_vous.data.repository.RendezVousRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides and binds application dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRendezVousRepository(
        rendezVousRepositoryImpl: RendezVousRepositoryImpl
    ): RendezVousRepository

    @Binds
    @Singleton
    abstract fun bindLogRepository(
        logRepositoryImpl: LogRepositoryImpl
    ): LogRepository

    companion object {
        
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
