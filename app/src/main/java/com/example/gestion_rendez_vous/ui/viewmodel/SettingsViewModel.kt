package com.example.gestion_rendez_vous.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_rendez_vous.data.model.UserActionLog
import com.example.gestion_rendez_vous.data.repository.LogRepository
import com.example.gestion_rendez_vous.data.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for global application settings, including
 * dark theme configuration and security action logging utilities.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securityManager: SecurityManager,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _logsList = MutableStateFlow<List<UserActionLog>>(emptyList())
    val logsList: StateFlow<List<UserActionLog>> = _logsList.asStateFlow()

    init {
        _isDarkMode.value = securityManager.isDarkModeEnabled()
        loadLogs()
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        securityManager.setDarkMode(enabled)
        logRepository.logAction("PREFERENCES", "Thème mis à jour (Sombre=$enabled)", "SUCCES")
    }

    fun loadLogs() {
        viewModelScope.launch {
            _logsList.value = logRepository.getLogs()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            logRepository.clearLogs()
            _logsList.value = emptyList()
            logRepository.logAction("SECURITE", "Journal d'audit effacé par l'utilisateur", "SUCCES")
        }
    }
}
