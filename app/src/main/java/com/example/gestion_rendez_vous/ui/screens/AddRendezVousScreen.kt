package com.example.gestion_rendez_vous.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gestion_rendez_vous.ui.viewmodel.RdvUiState
import com.example.gestion_rendez_vous.ui.viewmodel.RendezVousViewModel

/**
 * Screen designed to register a new medical appointment.
 * Displays descriptive field errors and blocks double-submissions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRendezVousScreen(
    navController: NavController,
    viewModel: RendezVousViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val formErrors by viewModel.formErrors.collectAsState()

    val patient by viewModel.nomPatientInput.collectAsState()
    val medecin by viewModel.nomMedecinInput.collectAsState()
    val specialite by viewModel.specialiteInput.collectAsState()
    val date by viewModel.dateInput.collectAsState()
    val heure by viewModel.heureInput.collectAsState()
    val description by viewModel.descriptionInput.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearForm()
        viewModel.clearState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau Rendez-vous", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top error notification card
                if (uiState is RdvUiState.Error) {
                    val errorMsg = (uiState as RdvUiState.Error).message
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp
                        )
                    }
                }

                // Nom complet patient
                OutlinedTextField(
                    value = patient,
                    onValueChange = { viewModel.nomPatientInput.value = it },
                    label = { Text("Nom complet du patient") },
                    isError = formErrors.containsKey("patient"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                formErrors["patient"]?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Nom médecin
                OutlinedTextField(
                    value = medecin,
                    onValueChange = { viewModel.nomMedecinInput.value = it },
                    label = { Text("Nom du médecin") },
                    isError = formErrors.containsKey("medecin"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                formErrors["medecin"]?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Spécialité médicale
                OutlinedTextField(
                    value = specialite,
                    onValueChange = { viewModel.specialiteInput.value = it },
                    label = { Text("Spécialité médicale") },
                    isError = formErrors.containsKey("specialite"),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                formErrors["specialite"]?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Date & Time Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { viewModel.dateInput.value = it },
                            label = { Text("Date (jj/mm/aaaa)") },
                            isError = formErrors.containsKey("date"),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        formErrors["date"]?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = heure,
                            onValueChange = { viewModel.heureInput.value = it },
                            label = { Text("Heure (hh:mm)") },
                            isError = formErrors.containsKey("heure"),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        formErrors["heure"]?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.descriptionInput.value = it },
                    label = { Text("Description / Symptômes / Remarques") },
                    isError = formErrors.containsKey("description"),
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                formErrors["description"]?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create Action
                val isLoading = uiState is RdvUiState.Loading
                Button(
                    onClick = {
                        if (!isLoading) {
                            viewModel.createRendezVous(context) {
                                navController.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Confirmer le rendez-vous", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
