package com.bharath.homeforge.ui

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.reminders.ReminderScheduler
import com.bharath.homeforge.ui.screens.HistoryScreen
import com.bharath.homeforge.ui.screens.LogScreen
import com.bharath.homeforge.ui.screens.OnboardingScreen
import com.bharath.homeforge.ui.screens.ProgressScreen
import com.bharath.homeforge.ui.screens.RoutinesScreen
import com.bharath.homeforge.ui.screens.SettingsScreen

private const val HISTORY_ROUTE = "history"

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Routines("routines", "Routines", Icons.Filled.FitnessCenter),
    Log("log", "Log", Icons.Filled.EditNote),
    Progress("progress", "Progress", Icons.Filled.ShowChart),
    Settings("settings", "Settings", Icons.Filled.Settings),
}

@Composable
fun HomeForgeApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var showOnboarding by remember { mutableStateOf(!prefs.getBoolean("onboarded", false)) }
    var startRoute by remember { mutableStateOf(Tab.Routines.route) }

    LaunchedEffect(Unit) { ReminderScheduler.schedule(context) }

    if (showOnboarding) {
        OnboardingScreen(
            onDone = { openSettings, goal ->
                UserPrefsRepository.get(context).update { it.copy(goal = goal) }
                prefs.edit().putBoolean("onboarded", true).apply()
                if (openSettings) startRoute = Tab.Settings.route
                showOnboarding = false
            },
        )
    } else {
        MainScaffold(startRoute)
    }
}

@Composable
private fun MainScaffold(startRoute: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route ||
                            (tab == Tab.Progress && currentRoute == HISTORY_ROUTE),
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
            startDestination = startRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable(Tab.Routines.route) { RoutinesScreen() }
            composable(Tab.Log.route) { LogScreen() }
            composable(Tab.Progress.route) {
                ProgressScreen(onOpenHistory = { navController.navigate(HISTORY_ROUTE) })
            }
            composable(Tab.Settings.route) { SettingsScreen() }
            composable(HISTORY_ROUTE) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
