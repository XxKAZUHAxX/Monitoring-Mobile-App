package com.example.lessonmonitor.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.lessonmonitor.ui.attendance.LessonAttendanceScreen
import com.example.lessonmonitor.ui.category.CategoryFormScreen
import com.example.lessonmonitor.ui.dashboard.DashboardScreen
import com.example.lessonmonitor.ui.lesson.LessonFormScreen
import com.example.lessonmonitor.ui.lesson.LessonInfoScreen
import com.example.lessonmonitor.ui.lesson.LessonsListScreen
import com.example.lessonmonitor.ui.search.SearchScreen
import com.example.lessonmonitor.ui.student.StudentDetailScreen
import com.example.lessonmonitor.ui.student.StudentEnrollmentScreen
import com.example.lessonmonitor.ui.student.StudentFormScreen

/**
 * Dashboard tab: Categories -> Lessons List (Lessons + Students tabs) ->
 * Lesson Attendance / Lesson Info / Student Enrollment / Student Detail,
 * plus Search. Routes are unique across the whole graph regardless of which
 * nested block declares them.
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
                onLessonClick = { lessonId -> navController.navigate(Routes.lessonAttendance(lessonId)) },
                onLessonInfoClick = { lessonId -> navController.navigate(Routes.lessonInfo(lessonId)) },
                onAddLesson = { navController.navigate(Routes.lessonForm(categoryId)) },
                onAddStudent = { navController.navigate(Routes.studentEnrollment(categoryId)) },
                onStudentClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) }
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
            route = Routes.LESSON_INFO_PATTERN,
            arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: Routes.NEW_ID
            LessonInfoScreen(
                lessonId = lessonId,
                onEditClick = { navController.navigate(Routes.lessonForm(0L, lessonId)) }
            )
        }
        composable(
            route = Routes.LESSON_ATTENDANCE_PATTERN,
            arguments = listOf(navArgument("lessonId") { type = NavType.LongType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "lessonmonitor://lesson/{lessonId}/attendance" }
            )
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: Routes.NEW_ID
            LessonAttendanceScreen(lessonId = lessonId)
        }
        composable(
            route = Routes.STUDENT_ENROLLMENT_PATTERN,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: Routes.NEW_ID
            StudentEnrollmentScreen(
                categoryId = categoryId,
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
        composable(Routes.SEARCH) {
            SearchScreen(
                onCategoryResultClick = { categoryId -> navController.navigate(Routes.lessonsList(categoryId)) },
                onLessonResultClick = { lessonId -> navController.navigate(Routes.lessonAttendance(lessonId)) },
                onStudentResultClick = { studentId -> navController.navigate(Routes.studentDetail(studentId)) }
            )
        }
    }
}
