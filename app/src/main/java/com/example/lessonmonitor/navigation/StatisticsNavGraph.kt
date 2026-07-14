package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.lessonmonitor.ui.statistics.StatisticsScreen

fun NavGraphBuilder.statisticsGraph(navController: NavHostController) {
    navigation(startDestination = Routes.STATISTICS, route = Routes.STATISTICS_GRAPH) {
        composable(Routes.STATISTICS) {
            StatisticsScreen(
                onStudentClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) },
                onLessonClick = { lessonId -> navController.navigate(Routes.lessonDetail(lessonId)) }
            )
        }
    }
}
