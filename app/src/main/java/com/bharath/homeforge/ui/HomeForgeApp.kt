package com.bharath.homeforge.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bharath.homeforge.ui.screens.LogScreen
import com.bharath.homeforge.ui.screens.ProgressScreen
import com.bharath.homeforge.ui.screens.RoutinesScreen
import com.bharath.homeforge.ui.screens.SettingsScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Routines("routines", "Routines", Icons.Filled.FitnessCenter),
    Log("log", "Log", Icons.Filled.EditNote),
    Progress("progress", "Progress", Icons.Filled.ShowChart),
    Settings("settings", "Settings", Icons.Filled.Settings),
}

@Composable
fun HomeForgeApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Routines.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Tab.Routines.route) { RoutinesScreen() }
            composable(Tab.Log.route) { LogScreen() }
            composable(Tab.Progress.route) { ProgressScreen() }
            composable(Tab.Settings.route) { SettingsScreen() }
        }
    }
}
