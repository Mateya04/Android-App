package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.LoanWithBorrower
import com.example.ui.viewmodels.MainViewModel
import com.example.utils.formatCurrency

@Composable
fun DashboardScreen(navController: NavController, viewModel: MainViewModel) {
    val allLoans by viewModel.allLoans.collectAsState()
    val activeLoans by viewModel.activeLoans.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currency = settings?.currency ?: "K"

    val totalLent = allLoans.sumOf { it.loan.amount }
    val totalCollected = allLoans.sumOf { it.loan.amountPaid }
    val totalOutstanding = activeLoans.sumOf { it.loan.remainingBalance }
    
    val now = System.currentTimeMillis()
    val sevenDays = 7L * 24 * 60 * 60 * 1000
    
    val overdueCount = activeLoans.count { it.loan.dueDate < now }
    val dueSoonCount = activeLoans.count { it.loan.dueDate in now..(now + sevenDays) }
    val fullyPaidCount = allLoans.count { it.loan.isPaid }

    MainScaffold(
        navController = navController,
        title = "Dashboard",
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_loan") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Loan")
            }
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                currentUser?.let { user ->
                    Text("Welcome, ${user.fullName}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text("Overview", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryCard(title = "Total Lent", amount = formatCurrency(totalLent, currency), modifier = Modifier.weight(1f))
                    SummaryCard(title = "Collected", amount = formatCurrency(totalCollected, currency), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                SummaryCard(title = "Outstanding Balance", amount = formatCurrency(totalOutstanding, currency), modifier = Modifier.fillMaxWidth())
            }

            item {
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusChip("Active: ${activeLoans.size}")
                    StatusChip("Due Soon: $dueSoonCount", MaterialTheme.colorScheme.tertiaryContainer)
                    StatusChip("Overdue: $overdueCount", MaterialTheme.colorScheme.errorContainer)
                }
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip("Fully Paid: $fullyPaidCount", MaterialTheme.colorScheme.secondaryContainer)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Loans", style = MaterialTheme.typography.titleLarge)
            }

            items(allLoans.take(5)) { loan ->
                LoanItem(loan, currency) {
                    navController.navigate("loan_detail/${loan.loan.id}")
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StatusChip(text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color,
        modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun LoanItem(loanWithBorrower: LoanWithBorrower, currency: String, onClick: () -> Unit) {
    val loan = loanWithBorrower.loan
    val borrower = loanWithBorrower.borrower
    
    val now = System.currentTimeMillis()
    val sevenDays = 7L * 24 * 60 * 60 * 1000
    
    val statusColor = when {
        loan.isPaid -> androidx.compose.ui.graphics.Color(0xFF388E3C) // Green
        loan.dueDate < now -> androidx.compose.ui.graphics.Color(0xFFD32F2F) // Red
        loan.dueDate < now + (24L * 60 * 60 * 1000) -> androidx.compose.ui.graphics.Color(0xFFF57C00) // Orange
        loan.dueDate < now + sevenDays -> androidx.compose.ui.graphics.Color(0xFFFBC02D) // Yellow
        else -> androidx.compose.ui.graphics.Color(0xFF1976D2) // Blue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = statusColor,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(borrower.fullName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(if (loan.isPaid) "Paid" else "Due: ${com.example.utils.formatDate(loan.dueDate)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(formatCurrency(loan.amount, currency), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (!loan.isPaid) {
                    Text("Bal: ${formatCurrency(loan.remainingBalance, currency)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
