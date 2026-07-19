package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.viewmodels.MainViewModel

@Composable
fun LoansScreen(navController: NavController, viewModel: MainViewModel) {
    val loans by viewModel.allLoans.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val currency = settings?.currency ?: "K"

    MainScaffold(
        navController = navController,
        title = "All Loans"
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(loans) { loan ->
                LoanItem(loan, currency) {
                    navController.navigate("loan_detail/${loan.loan.id}")
                }
            }
        }
    }
}
