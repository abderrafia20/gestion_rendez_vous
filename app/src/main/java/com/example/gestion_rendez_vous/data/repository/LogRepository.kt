package com.example.gestion_rendez_vous.data.repository

import com.example.gestion_rendez_vous.data.model.UserActionLog

/**
 * Interface defining operations for logging security events.
 */
interface LogRepository {
    fun logAction(action: String, details: String, status: String)
    fun getLogs(): List<UserActionLog>
    fun clearLogs()
}
