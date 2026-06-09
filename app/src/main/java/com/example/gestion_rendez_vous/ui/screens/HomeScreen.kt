package com.example.gestion_rendez_vous.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gestion_rendez_vous.data.model.RendezVous
import com.example.gestion_rendez_vous.ui.navigation.Screen
import com.example.gestion_rendez_vous.ui.viewmodel.AuthViewModel
import com.example.gestion_rendez_vous.ui.viewmodel.RendezVousViewModel

/**
 * Primary Dashboard Screen displaying the user's appointments.
 * Handles dynamic reminders permission asking and filters appointments list reactively.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    rdvViewModel: RendezVousViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val rdvList by rdvViewModel.rendezvousList.collectAsState()
    
    val searchQuery by rdvViewModel.searchQuery.collectAsState()
    val dateFilter by rdvViewModel.dateFilter.collectAsState()
    val sortByDoctor by rdvViewModel.sortByDoctor.collectAsState()

    // Request notification permission dynamically on launch for Android 13+ (POST_NOTIFICATIONS)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handle outcome if needed for auditing or preferences updates
    }

    LaunchedEffect(currentUser) {
        currentUser?.let {
            rdvViewModel.setUserId(it.uid)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MediSecure RDV", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Parametres.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Profil.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddRendezVous.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer un rendez-vous")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Welcome section
            Text(
                text = "Bonjour, ${currentUser?.nom ?: "Patient"}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Gérez vos consultations médicales en toute sécurité.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search query text input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { rdvViewModel.searchQuery.value = it },
                placeholder = { Text("Rechercher médecin, patient, spécialité...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search modifiers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar text-field filter
                OutlinedTextField(
                    value = dateFilter,
                    onValueChange = { rdvViewModel.dateFilter.value = it },
                    placeholder = { Text("jj/mm/aaaa") },
                    label = { Text("Filtrer par date") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    trailingIcon = {
                        if (dateFilter.isNotEmpty()) {
                            IconButton(onClick = { rdvViewModel.dateFilter.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer filtre")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1.5f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sort toggle button
                Button(
                    onClick = { rdvViewModel.sortByDoctor.value = !sortByDoctor },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sortByDoctor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (sortByDoctor) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(
                        imageVector = if (sortByDoctor) Icons.Default.ArrowDownward else Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tri Dr.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appointments list
            if (rdvList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || dateFilter.isNotEmpty()) 
                                "Aucun rendez-vous ne correspond à vos filtres." 
                            else 
                                "Vous n'avez aucun rendez-vous programmé.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(rdvList) { rdv ->
                        RendezVousCard(
                            rdv = rdv,
                            onClick = { navController.navigate(Screen.DetailsRendezVous.createRoute(rdv.id)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card representing a medical appointment.
 */
@Composable
fun RendezVousCard(rdv: RendezVous, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Patient : ${rdv.nomPatient}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rdv.heure,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Dr. ${rdv.nomMedecin} (${rdv.specialite})",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (rdv.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rdv.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = rdv.date,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = "Détails →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
