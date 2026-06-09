package com.example.gestion_rendez_vous.data.repository

import com.example.gestion_rendez_vous.data.model.UserActionLog
import com.example.gestion_rendez_vous.data.security.SecurityManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [LogRepository] delegating storage and retrieval to [SecurityManager].
 */
@Singleton
class LogRepositoryImpl @Inject constructor(
    private val securityManager: SecurityManager
) : LogRepository {

    override fun logAction(action: String, details: String, status: String) {
        securityManager.addLog(action, details, status)
    }

    override fun getLogs(): List<UserActionLog> {
        // Retrieve and return logs sorted in descending order of time (newest first)
        return securityManager.getLogs().sortedByDescending { it.timestamp }
    }

    override fun clearLogs() {
        securityManager.clearLogs()
    }
}
