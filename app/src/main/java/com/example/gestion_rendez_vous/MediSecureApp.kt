package com.example.gestion_rendez_vous

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class. Annotated with [HiltAndroidApp] to kick off Hilt's code generation.
 * Registers notification channels on startup.
 */
@HiltAndroidApp
class MediSecureApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Notification channels are required starting from Android Oreo (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "medisecure_rdv_reminders"
            val name = "Rappels de Rendez-vous"
            val descriptionText = "Notifications de rappel pour vos rendez-vous médicaux"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
