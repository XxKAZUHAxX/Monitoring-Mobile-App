package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.lessonmonitor.ui.export.BackupRestoreScreen
import com.example.lessonmonitor.ui.export.ExportScreen
import com.example.lessonmonitor.ui.settings.SettingsScreen

fun NavGraphBuilder.settingsGraph(navController: NavHostController, rootNavController: NavHostController) {
    navigation(startDestination = Routes.SETTINGS, route = Routes.SETTINGS_GRAPH) {
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onExportClick = { navController.navigate(Routes.EXPORT) },
                onBackupRestoreClick = { navController.navigate(Routes.BACKUP_RESTORE) },
                onLoggedOut = {
                    // Clears the session flag happens in the real ViewModel (User Account
                    // milestone); here we just prove the nav graph can pop all the way
                    // back out of `main` to the auth flow.
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.EXPORT) {
            ExportScreen()
        }
        composable(Routes.BACKUP_RESTORE) {
            BackupRestoreScreen()
        }
    }
}
