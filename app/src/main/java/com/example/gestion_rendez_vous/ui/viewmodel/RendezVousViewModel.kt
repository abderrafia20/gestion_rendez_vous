package com.example.gestion_rendez_vous.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestion_rendez_vous.data.model.RendezVous
import com.example.gestion_rendez_vous.data.repository.LogRepository
import com.example.gestion_rendez_vous.data.repository.RendezVousRepository
import com.example.gestion_rendez_vous.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * UI State for appointment database actions.
 */
sealed interface RdvUiState {
    object Idle : RdvUiState
    object Loading : RdvUiState
    object Success : RdvUiState
    data class Error(val message: String) : RdvUiState
}

/**
 * ViewModel managing appointment CRUD, forms, search queries, sorting and filtering.
 */
@HiltViewModel
class RendezVousViewModel @Inject constructor(
    private val rdvRepository: RendezVousRepository,
    private val logRepository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RdvUiState>(RdvUiState.Idle)
    val uiState: StateFlow<RdvUiState> = _uiState.asStateFlow()

    // Form inputs
    val nomPatientInput = MutableStateFlow("")
    val nomMedecinInput = MutableStateFlow("")
    val specialiteInput = MutableStateFlow("")
    val dateInput = MutableStateFlow("")
    val heureInput = MutableStateFlow("")
    val descriptionInput = MutableStateFlow("")

    // Form error state mapping
    private val _formErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val formErrors: StateFlow<Map<String, String>> = _formErrors.asStateFlow()

    // Filter controls
    val searchQuery = MutableStateFlow("")
    val dateFilter = MutableStateFlow("")
    val sortByDoctor = MutableStateFlow(false)

    private val _currentUserId = MutableStateFlow("")

    /**
     * Combined flow representing the reactive appointments list,
     * applying searches, date filtering, and doctor sorting in parallel.
     */
    val rendezvousList: StateFlow<List<RendezVous>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isEmpty()) flowOf(emptyList())
            else rdvRepository.getRendezVousFlow(userId)
        }
        .combine(searchQuery) { list, query ->
            if (query.isEmpty()) list
            else list.filter {
                it.nomPatient.contains(query, ignoreCase = true) ||
                it.nomMedecin.contains(query, ignoreCase = true) ||
                it.specialite.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
        .combine(dateFilter) { list, date ->
            if (date.isEmpty()) list
            else list.filter { it.date == date }
        }
        .combine(sortByDoctor) { list, sort ->
            if (sort) list.sortedBy { it.nomMedecin }
            else list
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedRdv = MutableStateFlow<RendezVous?>(null)
    val selectedRdv: StateFlow<RendezVous?> = _selectedRdv.asStateFlow()

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun loadRendezVous(id: String) {
        viewModelScope.launch {
            _uiState.value = RdvUiState.Loading
            rdvRepository.getRendezVousById(id)
                .onSuccess { rdv ->
                    _selectedRdv.value = rdv
                    if (rdv != null) {
                        nomPatientInput.value = rdv.nomPatient
                        nomMedecinInput.value = rdv.nomMedecin
                        specialiteInput.value = rdv.specialite
                        dateInput.value = rdv.date
                        heureInput.value = rdv.heure
                        descriptionInput.value = rdv.description
                    }
                    _uiState.value = RdvUiState.Success
                }
                .onFailure {
                    _uiState.value = RdvUiState.Error("Impossible de charger les détails du rendez-vous.")
                }
        }
    }

    /**
     * Validates fields for emptiness, formats (dd/MM/yyyy and HH:mm), and script injections.
     */
    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        val patient = nomPatientInput.value.trim()
        val medecin = nomMedecinInput.value.trim()
        val specialite = specialiteInput.value.trim()
        val date = dateInput.value.trim()
        val heure = heureInput.value.trim()
        val description = descriptionInput.value.trim()

        if (patient.isEmpty()) errors["patient"] = "Le nom du patient est requis."
        else if (hasUnsafeCharacters(patient)) errors["patient"] = "Le nom contient des caractères interdits."

        if (medecin.isEmpty()) errors["medecin"] = "Le nom du médecin est requis."
        else if (hasUnsafeCharacters(medecin)) errors["medecin"] = "Le nom contient des caractères interdits."

        if (specialite.isEmpty()) errors["specialite"] = "La spécialité médicale est requise."
        else if (hasUnsafeCharacters(specialite)) errors["specialite"] = "La spécialité contient des caractères interdits."

        if (hasUnsafeCharacters(description)) errors["description"] = "La description contient des caractères interdits."

        // Validate Date Format (dd/MM/yyyy)
        if (date.isEmpty()) {
            errors["date"] = "La date est requise."
        } else {
            val dateRegex = Regex("^\\d{2}/\\d{2}/\\d{4}\$")
            if (!dateRegex.matches(date)) {
                errors["date"] = "Format requis : jj/mm/aaaa"
            } else {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.isLenient = false
                    sdf.parse(date)
                } catch (e: Exception) {
                    errors["date"] = "Date calendrier invalide."
                }
            }
        }

        // Validate Time Format (HH:mm)
        if (heure.isEmpty()) {
            errors["heure"] = "L'heure est requise."
        } else {
            val timeRegex = Regex("^\\d{2}:\\d{2}\$")
            if (!timeRegex.matches(heure)) {
                errors["heure"] = "Format requis : hh:mm"
            } else {
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    sdf.isLenient = false
                    sdf.parse(heure)
                } catch (e: Exception) {
                    errors["heure"] = "Heure calendrier invalide."
                }
            }
        }

        _formErrors.value = errors
        return errors.isEmpty()
    }

    private fun hasUnsafeCharacters(input: String): Boolean {
        val unsafeSequences = listOf("<", ">", "\"", "'", "`", "&", ";", "--", "/*", "*/")
        return unsafeSequences.any { input.contains(it) }
    }

    fun createRendezVous(context: Context, onSuccess: () -> Unit) {
        if (!validateForm()) {
            logRepository.logAction("CREATION_RDV", "Échec de validation des données d'entrée", "ECHEC")
            return
        }

        _uiState.value = RdvUiState.Loading
        viewModelScope.launch {
            val newRdv = RendezVous(
                userId = _currentUserId.value,
                nomPatient = nomPatientInput.value.trim(),
                nomMedecin = nomMedecinInput.value.trim(),
                specialite = specialiteInput.value.trim(),
                date = dateInput.value.trim(),
                heure = heureInput.value.trim(),
                description = descriptionInput.value.trim()
            )
            rdvRepository.createRendezVous(newRdv)
                .onSuccess { rdvId ->
                    _uiState.value = RdvUiState.Success
                    // Register local heads-up reminder trigger
                    NotificationHelper.scheduleReminder(context, newRdv.copy(id = rdvId))
                    logRepository.logAction("CREATION_RDV", "Rendez-vous créé ($rdvId)", "SUCCES")
                    clearForm()
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = RdvUiState.Error(exception.localizedMessage ?: "Une erreur est survenue.")
                    logRepository.logAction("CREATION_RDV", "Échec insertion Firestore: ${exception.message}", "ECHEC")
                }
        }
    }

    fun updateRendezVous(context: Context, rdvId: String, onSuccess: () -> Unit) {
        if (!validateForm()) {
            logRepository.logAction("MODIFICATION_RDV", "Échec de validation lors de l'édition", "ECHEC")
            return
        }

        _uiState.value = RdvUiState.Loading
        viewModelScope.launch {
            val updatedRdv = RendezVous(
                id = rdvId,
                userId = _currentUserId.value,
                nomPatient = nomPatientInput.value.trim(),
                nomMedecin = nomMedecinInput.value.trim(),
                specialite = specialiteInput.value.trim(),
                date = dateInput.value.trim(),
                heure = heureInput.value.trim(),
                description = descriptionInput.value.trim()
            )
            rdvRepository.updateRendezVous(updatedRdv)
                .onSuccess {
                    _uiState.value = RdvUiState.Success
                    // Reschedule updated appointment reminder
                    NotificationHelper.cancelReminder(context, rdvId)
                    NotificationHelper.scheduleReminder(context, updatedRdv)
                    logRepository.logAction("MODIFICATION_RDV", "Rendez-vous mis à jour ($rdvId)", "SUCCES")
                    clearForm()
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = RdvUiState.Error(exception.localizedMessage ?: "Échec de la modification.")
                    logRepository.logAction("MODIFICATION_RDV", "Échec édition Firestore pour $rdvId: ${exception.message}", "ECHEC")
                }
        }
    }

    fun deleteRendezVous(context: Context, rdvId: String, onSuccess: () -> Unit = {}) {
        _uiState.value = RdvUiState.Loading
        viewModelScope.launch {
            rdvRepository.deleteRendezVous(rdvId)
                .onSuccess {
                    _uiState.value = RdvUiState.Success
                    // Cancel associated Alarm reminder
                    NotificationHelper.cancelReminder(context, rdvId)
                    logRepository.logAction("SUPPRESSION_RDV", "Rendez-vous supprimé ($rdvId)", "SUCCES")
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = RdvUiState.Error(exception.localizedMessage ?: "Échec de la suppression.")
                    logRepository.logAction("SUPPRESSION_RDV", "Échec suppression $rdvId: ${exception.message}", "ECHEC")
                }
        }
    }

    fun clearForm() {
        nomPatientInput.value = ""
        nomMedecinInput.value = ""
        specialiteInput.value = ""
        dateInput.value = ""
        heureInput.value = ""
        descriptionInput.value = ""
        _formErrors.value = emptyMap()
    }

    fun clearState() {
        _uiState.value = RdvUiState.Idle
    }
}
