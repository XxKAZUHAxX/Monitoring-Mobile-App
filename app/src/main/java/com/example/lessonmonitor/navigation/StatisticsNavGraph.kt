package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.lessonmonitor.ui.statistics.StatisticsScreen

fun NavGraphBuilder.statisticsGraph(navController: NavHostController) {
    navigation(startDestination = Routes.STATISTICS, route = Routes.STATISTICS_GRAPH) {
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onCategoryClick = { categoryId -> navController.navigate(Routes.categoryStats(categoryId)) },
                onStudentClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) }
            )
        }
        composable(
            route = Routes.CATEGORY_STATS_PATTERN,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: Routes.NEW_ID
            StatisticsScreen(
                categoryId = categoryId,
                onCategoryClick = {},
                onStudentClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) }
            )
        }
    }
}
