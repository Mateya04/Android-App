package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(navController: NavController, viewModel: MainViewModel, loanId: Int) {
    val context = LocalContext.current
    val allLoans by viewModel.allLoans.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val currency = settings?.currency ?: "K"
    
    val loanWithBorrower = allLoans.find { it.loan.id == loanId }
    
    if (loanWithBorrower == null) {
        // Handle not found
        return
    }
    
    val loan = loanWithBorrower.loan
    val borrower = loanWithBorrower.borrower
    
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Loan", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Borrower: ${borrower.fullName}", style = MaterialTheme.typography.titleMedium)
                    Text("Phone: ${borrower.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Loan Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Amount Lent: ${com.example.utils.formatCurrency(loan.amount, currency)}")
                    Text("Interest (${loan.interestRate}%): ${com.example.utils.formatCurrency(loan.totalRepayment - loan.amount, currency)}")
                    Text("Total to Repay: ${com.example.utils.formatCurrency(loan.totalRepayment, currency)}", style = MaterialTheme.typography.titleSmall)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Amount Paid: ${com.example.utils.formatCurrency(loan.amountPaid, currency)}")
                    Text("Remaining: ${com.example.utils.formatCurrency(loan.remainingBalance, currency)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Due Date: ${com.example.utils.formatDate(loan.dueDate)}")
                }
            }
            
            if (!loan.isPaid) {
                Button(
                    onClick = { showPaymentDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Record Payment")
                }
            } else {
                StatusChip("Fully Paid", MaterialTheme.colorScheme.secondaryContainer)
            }
        }
        
        if (showPaymentDialog) {
            var paymentAmountStr by remember { mutableStateOf(loan.remainingBalance.toString()) }
            var paymentMethod by remember { mutableStateOf("Cash") }
            
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = { Text("Record Payment") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = paymentAmountStr,
                            onValueChange = { paymentAmountStr = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = { paymentMethod = it },
                            label = { Text("Method (e.g. Cash, Bank)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val pAmount = paymentAmountStr.toDoubleOrNull()
                            if (pAmount != null && pAmount > 0) {
                                if (pAmount > loan.remainingBalance) {
                                    Toast.makeText(context, "Amount exceeds remaining balance", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addPayment(
                                        loanId = loan.id,
                                        amount = pAmount,
                                        method = paymentMethod,
                                        notes = "",
                                        date = System.currentTimeMillis()
                                    ) {
                                        Toast.makeText(context, "Payment Recorded", Toast.LENGTH_SHORT).show()
                                        showPaymentDialog = false
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentDialog = false }) { Text("Cancel") }
                }
            )
        }
        
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Loan") },
                text = { Text("Are you sure you want to delete this loan? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteLoan(loan)
                            Toast.makeText(context, "Loan Deleted", Toast.LENGTH_SHORT).show()
                            showDeleteConfirmDialog = false
                            navController.popBackStack()
                        }
                    ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
