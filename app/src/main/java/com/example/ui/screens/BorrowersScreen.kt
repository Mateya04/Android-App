package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.Borrower
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowersScreen(navController: NavController, viewModel: MainViewModel) {
    val borrowers by viewModel.allBorrowers.collectAsState()
    var borrowerToDelete by remember { mutableStateOf<Borrower?>(null) }
    var borrowerToView by remember { mutableStateOf<Borrower?>(null) }
    val context = LocalContext.current

    MainScaffold(
        navController = navController,
        title = "Borrowers"
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(borrowers) { borrower ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { borrowerToView = borrower }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(borrower.fullName, style = MaterialTheme.typography.titleMedium)
                            Text(borrower.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { borrowerToDelete = borrower }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Borrower", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        
        borrowerToView?.let { borrower ->
            AlertDialog(
                onDismissRequest = { borrowerToView = null },
                title = { Text("Borrower Details") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Name: ${borrower.fullName}")
                        Text("Phone: ${borrower.phoneNumber}")
                        if (borrower.nationalId.isNotBlank()) {
                            Text("National ID: ${borrower.nationalId}")
                        }
                        if (borrower.address.isNotBlank()) {
                            Text("Address: ${borrower.address}")
                        }
                        if (borrower.notes.isNotBlank()) {
                            Text("Notes: ${borrower.notes}")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { borrowerToView = null }) { Text("Close") }
                }
            )
        }
        
        borrowerToDelete?.let { borrower ->
            AlertDialog(
                onDismissRequest = { borrowerToDelete = null },
                title = { Text("Delete Borrower") },
                text = { Text("Are you sure you want to delete ${borrower.fullName}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteBorrower(borrower) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            borrowerToDelete = null
                        }
                    ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { borrowerToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}
