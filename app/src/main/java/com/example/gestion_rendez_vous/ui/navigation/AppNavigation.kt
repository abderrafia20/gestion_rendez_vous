package com.example.gestion_rendez_vous.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gestion_rendez_vous.ui.screens.*

/**
 * Main Navigation Graph of the application.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.AddRendezVous.route) {
            AddRendezVousScreen(navController = navController)
        }
        
        composable(
            route = Screen.EditRendezVous.route,
            arguments = listOf(navArgument("rdvId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rdvId = backStackEntry.arguments?.getString("rdvId") ?: ""
            EditRendezVousScreen(navController = navController, rdvId = rdvId)
        }
        
        composable(
            route = Screen.DetailsRendezVous.route,
            arguments = listOf(navArgument("rdvId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rdvId = backStackEntry.arguments?.getString("rdvId") ?: ""
            DetailsRendezVousScreen(navController = navController, rdvId = rdvId)
        }
        
        composable(Screen.Profil.route) {
            ProfilScreen(navController = navController)
        }
        
        composable(Screen.Parametres.route) {
            ParametresScreen(navController = navController)
        }
    }
}
