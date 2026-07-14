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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * The post-login app shell: a bottom nav bar + its own internal NavHost with
 * one nested graph per tab (Dashboard/Calendar/Statistics/Settings), so each
 * tab keeps its own back stack/state when switching tabs — see PLAN.md §4.
 * [rootNavController] is threaded through to the Settings tab so "log out"
 * can pop all the way back to the auth graph.
 *
 * [mainScreenViewModel] has no UI state of its own — obtaining it via
 * `hiltViewModel()` here just triggers its one-time recurring-session
 * generation side effect (see [MainScreenViewModel]) exactly once per
 * app-open, scoped to this composable's back stack entry.
 */
@Composable
fun MainScreen(rootNavController: NavHostController, mainScreenViewModel: MainScreenViewModel = hiltViewModel()) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by innerNavController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.graphRoute } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            innerNavController.navigate(item.graphRoute) {
                                popUpTo(innerNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
            calendarGraph(innerNavController)
            statisticsGraph(innerNavController)
            settingsGraph(innerNavController, rootNavController)
        }
    }
}
