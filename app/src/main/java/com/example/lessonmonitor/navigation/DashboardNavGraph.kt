package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.lessonmonitor.ui.attendance.AttendanceSessionScreen
import com.example.lessonmonitor.ui.category.CategoryFormScreen
import com.example.lessonmonitor.ui.dashboard.DashboardScreen
import com.example.lessonmonitor.ui.lesson.LessonDetailScreen
import com.example.lessonmonitor.ui.lesson.LessonFormScreen
import com.example.lessonmonitor.ui.lesson.LessonsListScreen
import com.example.lessonmonitor.ui.search.SearchScreen
import com.example.lessonmonitor.ui.student.StudentDetailScreen
import com.example.lessonmonitor.ui.student.StudentFormScreen
import com.example.lessonmonitor.ui.student.StudentPickerScreen

/**
 * Dashboard tab: Categories -> Lessons -> Lesson Detail -> (roster/sessions),
 * plus the Student Picker/Form/Detail and Search screens that are reached
 * from this tab. `StudentDetail` is also navigated to from the Statistics
 * tab (see StatisticsNavGraph.kt) — routes are unique across the whole graph
 * regardless of which nested block declares them.
 */
fun NavGraphBuilder.dashboardGraph(navController: NavHostController) {
    navigation(startDestination = Routes.DASHBOARD, route = Routes.DASHBOARD_GRAPH) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onCategoryClick = { categoryId -> navController.navigate(Routes.lessonsList(categoryId)) },
                onAddCategory = { navController.navigate(Routes.categoryForm()) },
                onSearchClick = { navController.navigate(Routes.SEARCH) }
            )
        }
        composable(
            route = Routes.CATEGORY_FORM_PATTERN,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: Routes.NEW_ID
            CategoryFormScreen(categoryId = categoryId, onDone = { navController.popBackStack() })
        }
        composable(
            route = Routes.LESSONS_LIST_PATTERN,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: Routes.NEW_ID
            LessonsListScreen(
                categoryId = categoryId,
                onLessonClick = { lessonId -> navController.navigate(Routes.lessonDetail(lessonId)) },
                onAddLesson = { navController.navigate(Routes.lessonForm(categoryId)) }
            )
        }
        composable(
            route = Routes.LESSON_FORM_PATTERN,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("lessonId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: Routes.NEW_ID
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: Routes.NEW_ID
            LessonFormScreen(categoryId = categoryId, lessonId = lessonId, onDone = { navController.popBackStack() })
        }
        composable(
            route = Routes.LESSON_DETAIL_PATTERN,
            arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: Routes.NEW_ID
            LessonDetailScreen(
                lessonId = lessonId,
                onAddStudent = { navController.navigate(Routes.studentPicker(lessonId)) },
                onStudentClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) },
                onSessionClick = { sessionId -> navController.navigate(Routes.attendanceSession(lessonId, sessionId)) }
            )
        }
        composable(
            route = Routes.STUDENT_PICKER_PATTERN,
            arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: Routes.NEW_ID
            StudentPickerScreen(
                lessonId = lessonId,
                onCreateNewStudent = { navController.navigate(Routes.studentForm()) },
                onDone = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.STUDENT_FORM_PATTERN,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: Routes.NEW_ID
            StudentFormScreen(studentId = studentId, onDone = { navController.popBackStack() })
        }
        composable(
            route = Routes.STUDENT_DETAIL_PATTERN,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: Routes.NEW_ID
            StudentDetailScreen(studentId = studentId, onDeleted = { navController.popBackStack() })
        }
        composable(
            route = Routes.ATTENDANCE_SESSION_PATTERN,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.LongType },
                navArgument("sessionId") { type = NavType.LongType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "lessonmonitor://lesson/{lessonId}/session/{sessionId}" }
            )
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: Routes.NEW_ID
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: Routes.NEW_ID
            AttendanceSessionScreen(lessonId = lessonId, sessionId = sessionId)
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onCategoryResultClick = { categoryId -> navController.navigate(Routes.lessonsList(categoryId)) },
                onLessonResultClick = { lessonId -> navController.navigate(Routes.lessonDetail(lessonId)) },
                onStudentResultClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) }
            )
        }
    }
}
