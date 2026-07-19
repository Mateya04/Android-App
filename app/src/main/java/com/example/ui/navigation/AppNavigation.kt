package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.viewmodels.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) "login" else if (currentUser?.username == "admin") "admin_dashboard" else "dashboard"
    ) {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("register") { RegisterScreen(navController, viewModel) }
        
        // Admin App
        composable("admin_dashboard") { AdminDashboardScreen(navController, viewModel) }
        
        // Main App
        composable("dashboard") { DashboardScreen(navController, viewModel) }
        composable("add_loan") { AddLoanScreen(navController, viewModel) }
        composable("borrowers") { BorrowersScreen(navController, viewModel) }
        composable("loans") { LoansScreen(navController, viewModel) }
        composable("settings") { SettingsScreen(navController, viewModel) }
        // For a specific loan detail
        composable("loan_detail/{loanId}") { backStackEntry ->
            val loanId = backStackEntry.arguments?.getString("loanId")?.toIntOrNull()
            if (loanId != null) {
                LoanDetailScreen(navController, viewModel, loanId)
            }
        }
    }
}
