package com.example.lessonmonitor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * App-wide navigation root. Hosts the `auth` graph (Splash/CreateCredential/
 * Login — no bottom nav) and a single `main` destination that owns its own
 * nested NavHost + bottom navigation bar (see MainScreen.kt) once the user is
 * past the local login gate. See PLAN.md §4 for the full route design.
 */
@Composable
fun LessonMonitorNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.AUTH_GRAPH) {
        authGraph(navController)
        composable(Routes.MAIN) {
            MainScreen(rootNavController = navController)
        }
    }
}
