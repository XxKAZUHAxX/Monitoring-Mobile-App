package com.example.lessonmonitor.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** One entry per bottom-nav tab; [graphRoute] is the nested graph's route (see MainScreen.kt). */
data class BottomNavItem(
    val graphRoute: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD_GRAPH, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Routes.STATISTICS_GRAPH, "Statistics", Icons.Filled.Insights),
    BottomNavItem(Routes.SETTINGS_GRAPH, "Settings", Icons.Filled.Settings)
)
