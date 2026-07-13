package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.lessonmonitor.ui.calendar.CalendarScreen
import com.example.lessonmonitor.ui.calendar.DayAgendaScreen

fun NavGraphBuilder.calendarGraph(navController: NavHostController) {
    navigation(startDestination = Routes.CALENDAR, route = Routes.CALENDAR_GRAPH) {
        composable(Routes.CALENDAR) {
            CalendarScreen(onDayClick = { epochDay -> navController.navigate(Routes.dayAgenda(epochDay)) })
        }
        composable(
            route = Routes.DAY_AGENDA_PATTERN,
            arguments = listOf(navArgument("epochDay") { type = NavType.LongType })
        ) { backStackEntry ->
            val epochDay = backStackEntry.arguments?.getLong("epochDay") ?: 0L
            DayAgendaScreen(
                epochDay = epochDay,
                onSessionClick = { lessonId, sessionId ->
                    navController.navigate(Routes.attendanceSession(lessonId, sessionId))
                }
            )
        }
    }
}
