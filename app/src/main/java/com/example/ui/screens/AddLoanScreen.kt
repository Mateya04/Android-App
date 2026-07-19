package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
fun AddLoanScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    
    var phone by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var borrowerNotes by remember { mutableStateOf("") }
    
    var amountStr by remember { mutableStateOf("") }
    var durationWeeksStr by remember { mutableStateOf("1") }
    var interestRateStr by remember { mutableStateOf(settings?.rate1Week?.toString() ?: "10.0") }
    
    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val durationWeeks = durationWeeksStr.toIntOrNull() ?: 1
    val interestRate = interestRateStr.toDoubleOrNull() ?: 0.0
    
    val interestAmount = amount * (interestRate / 100.0)
    val totalRepayment = amount + interestAmount
    
    LaunchedEffect(durationWeeks) {
        settings?.let {
            interestRateStr = when (durationWeeks) {
                1 -> it.rate1Week.toString()
                2 -> it.rate2Weeks.toString()
                3 -> it.rate3Weeks.toString()
                4 -> it.rate4Weeks.toString()
                else -> it.rate4Weeks.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Loan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text("Borrower Information", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = nationalId,
                onValueChange = { nationalId = it },
                label = { Text("National ID (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Loan Information", style = MaterialTheme.typography.titleMedium)
            
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Loan Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            OutlinedTextField(
                value = com.example.utils.formatDate(datePickerState.selectedDateMillis ?: System.currentTimeMillis()),
                onValueChange = {},
                label = { Text("Date Issued") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            Text("Duration (Weeks)", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(1, 2, 3, 4).forEach { week ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = durationWeeksStr == week.toString(),
                            onClick = { durationWeeksStr = week.toString() }
                        )
                        Text("$week")
                    }
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Calculated Summary", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Interest Amount: ${com.example.utils.formatCurrency(interestAmount, settings?.currency ?: "K")}")
                    Text("Total Repayment: ${com.example.utils.formatCurrency(totalRepayment, settings?.currency ?: "K")}")
                }
            }
            
            Button(
                onClick = {
                    if (phone.isNotBlank() && fullName.isNotBlank() && amount > 0) {
                        viewModel.addLoan(
                            phone = phone,
                            fullName = fullName,
                            nationalId = nationalId,
                            address = address,
                            notes = borrowerNotes,
                            amount = amount,
                            durationWeeks = durationWeeks,
                            interestRate = interestRate,
                            dateIssued = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        ) {
                            Toast.makeText(context, "Loan added", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    } else {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Loan")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
