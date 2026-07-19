package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    title: String,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        if (currentRoute != "dashboard") {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Loans") },
                    label = { Text("Loans") },
                    selected = currentRoute == "loans",
                    onClick = {
                        if (currentRoute != "loans") {
                            navController.navigate("loans")
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "Borrowers") },
                    label = { Text("Borrowers") },
                    selected = currentRoute == "borrowers",
                    onClick = {
                        if (currentRoute != "borrowers") {
                            navController.navigate("borrowers")
                        }
                    }
                )
            }
        },
        floatingActionButton = floatingActionButton
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
