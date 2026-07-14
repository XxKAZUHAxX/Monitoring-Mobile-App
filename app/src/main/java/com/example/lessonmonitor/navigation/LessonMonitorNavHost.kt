package com.example.lessonmonitor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * App-wide navigation root. Hosts the `auth` graph (Splash/CreateCredential/
 * Login — no bottom nav) and a single `main` destination that owns its own
 * nested NavHost + bottom navigation bar (see MainScreen.kt) once the user is
 * past the local login gate. See PLAN.md §4 for the full route design.
 *
 * [onNavControllerReady] is called once when the [NavHostController] is
 * created so [MainActivity] can forward notification deep-link intents via
 * [NavHostController.handleDeepLink] when the app is already running.
 */
@Composable
fun LessonMonitorNavHost(
    navController: NavHostController = rememberNavController(),
    onNavControllerReady: (NavHostController) -> Unit = {}
) {
    LaunchedEffect(navController) { onNavControllerReady(navController) }

    NavHost(navController = navController, startDestination = Routes.AUTH_GRAPH) {
        authGraph(navController)
        composable(Routes.MAIN) {
            MainScreen(rootNavController = navController)
        }
    }
}
