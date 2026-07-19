package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val settings by viewModel.userSettings.collectAsState()
    val context = LocalContext.current
    
    var currency by remember(settings) { mutableStateOf(settings?.currency ?: "K") }
    var r1 by remember(settings) { mutableStateOf(settings?.rate1Week?.toString() ?: "10.0") }
    var r2 by remember(settings) { mutableStateOf(settings?.rate2Weeks?.toString() ?: "20.0") }
    var r3 by remember(settings) { mutableStateOf(settings?.rate3Weeks?.toString() ?: "30.0") }
    var r4 by remember(settings) { mutableStateOf(settings?.rate4Weeks?.toString() ?: "35.0") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            Text("Default Interest Rates (%)", style = MaterialTheme.typography.titleMedium)
            
            OutlinedTextField(
                value = r1, onValueChange = { r1 = it }, label = { Text("1 Week Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = r2, onValueChange = { r2 = it }, label = { Text("2 Weeks Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = r3, onValueChange = { r3 = it }, label = { Text("3 Weeks Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = r4, onValueChange = { r4 = it }, label = { Text("4+ Weeks Rate") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
            )
            
            var theme by remember(settings) { mutableStateOf(settings?.theme ?: "system") }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Preferences", style = MaterialTheme.typography.titleMedium)
            
            Text("App Theme", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = theme == "system", onClick = { theme = "system" })
                    Text("System")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = theme == "light", onClick = { theme = "light" })
                    Text("Light")
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = theme == "dark", onClick = { theme = "dark" })
                    Text("Dark")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currency, onValueChange = { currency = it }, label = { Text("Currency Symbol") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    viewModel.updateSettings(
                        currency = currency,
                        r1 = r1.toDoubleOrNull() ?: 10.0,
                        r2 = r2.toDoubleOrNull() ?: 20.0,
                        r3 = r3.toDoubleOrNull() ?: 30.0,
                        r4 = r4.toDoubleOrNull() ?: 35.0,
                        theme = theme
                    )
                    Toast.makeText(context, "Settings Saved", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Settings")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}
