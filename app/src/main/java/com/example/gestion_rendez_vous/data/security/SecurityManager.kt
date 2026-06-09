package com.example.gestion_rendez_vous.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.gestion_rendez_vous.data.model.UserActionLog
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages security-sensitive data stored locally.
 * Utilizes [EncryptedSharedPreferences] to ensure that no plain text data (such as themes, keys,
 * or user action logs) is accessible in cleartext, defending against snooping on rooted devices.
 */
@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "medisecure_pref_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LOGS = "security_logs"
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    /**
     * Appends a log of user operations to an encrypted file for security audits.
     */
    fun addLog(action: String, details: String, status: String) {
        val currentLogs = getLogs().toMutableList()
        val newLog = UserActionLog(
            timestamp = System.currentTimeMillis(),
            action = action,
            details = details,
            status = status
        )
        if (currentLogs.size >= 100) {
            currentLogs.removeAt(0) // Prevent storage bloating
        }
        currentLogs.add(newLog)
        
        try {
            val jsonArray = JSONArray()
            for (log in currentLogs) {
                val jsonObject = JSONObject().apply {
                    put("timestamp", log.timestamp)
                    put("action", log.action)
                    put("details", log.details)
                    put("status", log.status)
                }
                jsonArray.put(jsonObject)
            }
            sharedPreferences.edit().putString(KEY_LOGS, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Retrieves the list of actions stored in the encrypted storage.
     */
    fun getLogs(): List<UserActionLog> {
        val jsonString = sharedPreferences.getString(KEY_LOGS, null) ?: return emptyList()
        val logs = mutableListOf<UserActionLog>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                logs.add(
                    UserActionLog(
                        timestamp = jsonObject.getLong("timestamp"),
                        action = jsonObject.getString("action"),
                        details = jsonObject.getString("details"),
                        status = jsonObject.getString("status")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return logs
    }
    
    fun clearLogs() {
        sharedPreferences.edit().remove(KEY_LOGS).apply()
    }
}
