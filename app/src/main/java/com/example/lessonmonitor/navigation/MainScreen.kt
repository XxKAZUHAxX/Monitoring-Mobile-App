package com.example.lessonmonitor.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * The post-login app shell: a bottom nav bar + its own internal NavHost with
 * one nested graph per tab (Dashboard/Statistics/Settings), so each tab keeps
 * its own back stack/state when switching tabs.
 *
 * Tab re-tap pops the selected tab's inner back stack to its root destination.
 * Tapping the same tab when already on it does nothing else.
 */
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by innerNavController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                bottomNavItems.forEach { item ->
                    // selected is true when the current destination belongs to this tab's graph
                    val selected = currentDestination?.hierarchy?.any { it.route == item.graphRoute } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentDestination == null) return@NavigationBarItem
                            if (selected) {
                                // Re-tap: pop to the graph root if we're deeper than it.
                                // If route == graphRoute we're already at root — do nothing.
                                if (currentDestination.route != item.graphRoute) {
                                    innerNavController.popBackStack(item.graphRoute, inclusive = false)
                                }
                            } else {
                                innerNavController.navigate(item.graphRoute) {
                                    popUpTo(innerNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Routes.DASHBOARD_GRAPH,
            modifier = Modifier.padding(innerPadding)
        ) {
            dashboardGraph(innerNavController)
            statisticsGraph(innerNavController)
            settingsGraph(innerNavController, rootNavController)
        }
    }
}
