package com.example.gestion_rendez_vous.notification

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gestion_rendez_vous.MainActivity

/**
 * BroadcastReceiver invoked by AlarmManager when an appointment reminder fires.
 * Builds and displays a local system notification.
 */
class RendezVousReminderReceiver : BroadcastReceiver() {

    @SuppressLint("NotificationPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val rdvId = intent.getStringExtra("rdv_id") ?: return
        val patient = intent.getStringExtra("patient") ?: "Patient"
        val medecin = intent.getStringExtra("medecin") ?: "Médecin"
        val specialite = intent.getStringExtra("specialite") ?: "Spécialité"
        val heure = intent.getStringExtra("heure") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Opens MainActivity when user taps notification
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            rdvId.hashCode(),
            clickIntent,
            pendingIntentFlags
        )

        // Using standard Android system drawable for compatibility
        val notification = NotificationCompat.Builder(context, "medisecure_rdv_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rappel de Rendez-vous")
            .setContentText("Rendez-vous de $patient avec Dr. $medecin à $heure.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Rappel : Votre rendez-vous pour $patient avec le Dr. $medecin ($specialite) est prévu à $heure. Veuillez vous présenter à l'heure."
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(rdvId.hashCode(), notification)
    }
}
