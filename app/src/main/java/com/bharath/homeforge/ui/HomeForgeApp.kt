package com.bharath.homeforge.ui

import android.content.Context
import android.net.Uri
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bharath.homeforge.data.UserPrefsRepository
import com.bharath.homeforge.reminders.ReminderScheduler
import com.bharath.homeforge.ui.screens.CalendarScreen
import com.bharath.homeforge.ui.screens.CreditsScreen
import com.bharath.homeforge.ui.screens.ExerciseDetailScreen
import com.bharath.homeforge.ui.screens.HistoryScreen
import com.bharath.homeforge.ui.screens.LogScreen
import com.bharath.homeforge.ui.screens.OnboardingScreen
import com.bharath.homeforge.ui.screens.ProgressScreen
import com.bharath.homeforge.ui.screens.RoutinesScreen
import com.bharath.homeforge.ui.screens.SettingsScreen

private const val HISTORY_ROUTE = "history"
private const val EXERCISE_ROUTE = "exercise"
private const val CREDITS_ROUTE = "credits"
private const val CALENDAR_ROUTE = "calendar"

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
            onDone = { openSettings, goal, program, level ->
                UserPrefsRepository.get(context).apply {
                    update { it.copy(goal = goal, level = level, lastAnnouncedLevel = level) }
                    choosePlan(program)
                }
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
            composable(Tab.Routines.route) {
                RoutinesScreen(
                    onOpenExercise = { navController.navigate("$EXERCISE_ROUTE/${Uri.encode(it)}") },
                    onOpenCalendar = { navController.navigate(CALENDAR_ROUTE) },
                )
            }
            composable(Tab.Log.route) {
                LogScreen(onOpenExercise = { navController.navigate("$EXERCISE_ROUTE/${Uri.encode(it)}") })
            }
            composable(Tab.Progress.route) {
                ProgressScreen(onOpenHistory = { navController.navigate(HISTORY_ROUTE) })
            }
            composable(Tab.Settings.route) {
                SettingsScreen(onOpenCredits = { navController.navigate(CREDITS_ROUTE) })
            }
            composable(HISTORY_ROUTE) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "$EXERCISE_ROUTE/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType }),
            ) { entry ->
                ExerciseDetailScreen(
                    name = entry.arguments?.getString("name").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
            composable(CREDITS_ROUTE) {
                CreditsScreen(onBack = { navController.popBackStack() })
            }
            composable(CALENDAR_ROUTE) {
                CalendarScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
