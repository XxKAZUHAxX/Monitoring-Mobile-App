package com.example.lessonmonitor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lessonmonitor.ui.dashboard.DashboardScreen

/**
 * Placeholder navigation graph for the initial project-setup milestone. Only
 * the Dashboard placeholder route exists so far; the full route set (auth
 * graph, category/lesson/attendance/student details, calendar, search,
 * statistics, settings, and the notification deep link) is designed in
 * PLAN.md §4 and will be wired up in the "Navigation skeleton" milestone.
 */
object Routes {
    const val DASHBOARD = "dashboard"
}

@Composable
fun LessonMonitorNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen()
        }
    }
}
