package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.lessonmonitor.ui.auth.CreateCredentialScreen
import com.example.lessonmonitor.ui.auth.LoginScreen
import com.example.lessonmonitor.ui.auth.SplashScreen

/**
 * Placeholder auth flow. Real session-check / credential logic (encrypted
 * DataStore + PBKDF2 verification + biometric prompt) is built in the "User
 * account" milestone; for now each screen exposes plain navigation actions so
 * the graph shape can be verified end to end.
 */
fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(startDestination = Routes.SPLASH, route = Routes.AUTH_GRAPH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNeedsLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNeedsCredentialSetup = {
                    navController.navigate(Routes.CREATE_CREDENTIAL) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onAlreadyLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CREATE_CREDENTIAL) {
            CreateCredentialScreen(
                onCredentialCreated = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                    }
                }
            )
        }
    }
}
