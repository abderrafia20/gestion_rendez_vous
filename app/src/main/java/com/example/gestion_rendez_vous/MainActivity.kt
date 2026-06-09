package com.example.gestion_rendez_vous

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.gestion_rendez_vous.ui.navigation.AppNavigation
import com.example.gestion_rendez_vous.ui.theme.MediSecureTheme
import com.example.gestion_rendez_vous.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity of the application.
 * Serves as the single activity container for our Jetpack Compose UI.
 * Annotated with [AndroidEntryPoint] to enable Hilt dependency injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge display support for modern UI padding behavior
        enableEdgeToEdge()
        
        setContent {
            // Observe the dark mode state from EncryptedSharedPreferences reactively
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            MediSecureTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }
}