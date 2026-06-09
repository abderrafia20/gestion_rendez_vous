package com.example.gestion_rendez_vous.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.gestion_rendez_vous.data.model.RendezVous
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Helper utility to schedule and cancel local reminders for appointments using AlarmManager.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "medisecure_rdv_reminders"

    /**
     * Schedules a local notification reminder.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleReminder(context: Context, rdv: RendezVous) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, RendezVousReminderReceiver::class.java).apply {
            putExtra("rdv_id", rdv.id)
            putExtra("patient", rdv.nomPatient)
            putExtra("medecin", rdv.nomMedecin)
            putExtra("specialite", rdv.specialite)
            putExtra("heure", rdv.heure)
        }

        // Required flags for PendingIntent starting from Android M/S (Immutable preference for security)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // We use the hashCode of the appointment ID as a unique requestCode to avoid collisions
        val requestCode = rdv.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, pendingIntentFlags)

        val triggerTime = getTriggerTime(rdv.date, rdv.heure)
        
        // Only schedule if the trigger time is in the future
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    /**
     * Cancels an existing scheduled notification reminder.
     */
    fun cancelReminder(context: Context, rdvId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, RendezVousReminderReceiver::class.java)
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val requestCode = rdvId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, pendingIntentFlags)
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Helper to compute trigger time. Tries to schedule 1 hour before the actual appointment.
     * If 1 hour before is in the past, defaults to the exact appointment start time.
     */
    private fun getTriggerTime(dateStr: String, timeStr: String): Long {
        return try {
            val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            format.timeZone = TimeZone.getDefault()
            val date = format.parse("$dateStr $timeStr")
            val appointmentTime = date?.time ?: 0L
            val oneHourBefore = appointmentTime - (60 * 60 * 1000)
            
            if (oneHourBefore > System.currentTimeMillis()) {
                oneHourBefore
            } else {
                appointmentTime
            }
        } catch (e: Exception) {
            0L
        }
    }
}
