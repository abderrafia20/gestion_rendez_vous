package com.example.gestion_rendez_vous.ui.navigation

/**
 * Sealed class representing application screens and their navigation routes.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddRendezVous : Screen("add_rdv")
    
    object EditRendezVous : Screen("edit_rdv/{rdvId}") {
        fun createRoute(rdvId: String) = "edit_rdv/$rdvId"
    }
    
    object DetailsRendezVous : Screen("details_rdv/{rdvId}") {
        fun createRoute(rdvId: String) = "details_rdv/$rdvId"
    }
    
    object Profil : Screen("profil")
    object Parametres : Screen("parametres")
}
