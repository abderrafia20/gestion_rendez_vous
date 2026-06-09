package com.example.gestion_rendez_vous.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_rendez_vous.data.model.User
import com.example.gestion_rendez_vous.data.repository.AuthRepository
import com.example.gestion_rendez_vous.data.repository.LogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

/**
 * UI State representation for Authentication operations.
 */
sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

/**
 * ViewModel managing the Authentication and session flows.
 * Performs rigorous validations, input sanitization, and security event auditing.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Form variables
    val nomInput = MutableStateFlow("")
    val emailInput = MutableStateFlow("")
    val passwordInput = MutableStateFlow("")

    private val _nomError = MutableStateFlow<String?>(null)
    val nomError: StateFlow<String?> = _nomError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // Email matching pattern
    private val emailPattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    init {
        // Observe token validation changes for automatic session logouts
        viewModelScope.launch {
            authRepository.observeSessionState().collect { user ->
                val previousUser = _currentUser.value
                _currentUser.value = user
                
                // Trigger auto-logout if token is revoked or session expires
                if (user == null && previousUser != null) {
                    logRepository.logAction(
                        "DECONNEXION_AUTO",
                        "Déconnexion automatique (token invalide/expiré)",
                        "SUCCES"
                    )
                    _uiState.value = AuthUiState.Error("Votre session a expiré. Veuillez vous reconnecter.")
                } else if (user != null) {
                    _uiState.value = AuthUiState.Success(user)
                }
            }
        }

        // Initialize logged in session checks on launch
        if (authRepository.isUserLoggedIn()) {
            _currentUser.value = authRepository.getCurrentUser()
            _currentUser.value?.let {
                _uiState.value = AuthUiState.Success(it)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            _emailError.value = "L'adresse email ne peut pas être vide."
            return false
        }
        if (!emailPattern.matcher(cleanEmail).matches()) {
            _emailError.value = "Format d'adresse email invalide."
            return false
        }
        if (hasUnsafeCharacters(cleanEmail)) {
            _emailError.value = "L'adresse email contient des caractères non autorisés."
            return false
        }
        _emailError.value = null
        return true
    }

    private fun validatePassword(password: String): Boolean {
        if (password.isEmpty()) {
            _passwordError.value = "Le mot de passe ne peut pas être vide."
            return false
        }
        if (password.length < 8) {
            _passwordError.value = "Le mot de passe doit comporter au moins 8 caractères."
            return false
        }
        if (hasUnsafeCharacters(password)) {
            _passwordError.value = "Le mot de passe contient des caractères non autorisés."
            return false
        }
        _passwordError.value = null
        return true
    }

    private fun validateNom(nom: String): Boolean {
        val cleanNom = nom.trim()
        if (cleanNom.isEmpty()) {
            _nomError.value = "Le nom ne peut pas être vide."
            return false
        }
        if (hasUnsafeCharacters(cleanNom)) {
            _nomError.value = "Le nom contient des caractères non autorisés."
            return false
        }
        _nomError.value = null
        return true
    }

    /**
     * Input Sanitization against SQL and Script Injection attempts.
     */
    private fun hasUnsafeCharacters(input: String): Boolean {
        val unsafeSequences = listOf("<", ">", "\"", "'", "`", "&", ";", "--", "/*", "*/")
        return unsafeSequences.any { input.contains(it) }
    }

    fun login() {
        val email = emailInput.value.trim()
        val password = passwordInput.value

        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)

        if (!isEmailValid || !isPasswordValid) {
            logRepository.logAction("CONNEXION", "Tentative échouée: format invalide ($email)", "ECHEC")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                    logRepository.logAction("CONNEXION", "Utilisateur connecté (${user.email})", "SUCCES")
                }
                .onFailure { exception ->
                    val errorMsg = when (exception.localizedMessage) {
                        "An internal error has occurred." -> "Erreur réseau. Veuillez réessayer."
                        else -> "Adresse email ou mot de passe incorrect."
                    }
                    _uiState.value = AuthUiState.Error(errorMsg)
                    logRepository.logAction("CONNEXION", "Identifiants incorrects pour $email: ${exception.message}", "ECHEC")
                }
        }
    }

    fun register() {
        val nom = nomInput.value.trim()
        val email = emailInput.value.trim()
        val password = passwordInput.value

        val isNomValid = validateNom(nom)
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password)

        if (!isNomValid || !isEmailValid || !isPasswordValid) {
            logRepository.logAction("INSCRIPTION", "Format d'inscription invalide ($email)", "ECHEC")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.register(nom, email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _uiState.value = AuthUiState.Success(user)
                    logRepository.logAction("INSCRIPTION", "Nouvel utilisateur inscrit (${user.email})", "SUCCES")
                }
                .onFailure { exception ->
                    val errorMsg = exception.localizedMessage ?: "Échec de l'inscription."
                    _uiState.value = AuthUiState.Error(errorMsg)
                    logRepository.logAction("INSCRIPTION", "Erreur création compte pour $email: ${exception.message}", "ECHEC")
                }
        }
    }

    fun resetPassword(email: String) {
        if (!validateEmail(email)) {
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess {
                    _uiState.value = AuthUiState.Idle
                    logRepository.logAction("REINITIALISATION_MDP", "Mail de réinitialisation envoyé à $email", "SUCCES")
                }
                .onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.localizedMessage ?: "Une erreur est survenue.")
                    logRepository.logAction("REINITIALISATION_MDP", "Échec envoi mail pour $email: ${exception.message}", "ECHEC")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val email = _currentUser.value?.email ?: "Inconnu"
            authRepository.logout()
            _currentUser.value = null
            _uiState.value = AuthUiState.Idle
            logRepository.logAction("DECONNEXION", "Utilisateur déconnecté ($email)", "SUCCES")
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}
