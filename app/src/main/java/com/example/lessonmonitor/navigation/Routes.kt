package com.example.lessonmonitor.navigation

/**
 * Central route registry. Route *patterns* are Navigation-Compose
 * template strings (e.g. "lesson_attendance/{lessonId}"); the helper functions
 * below build the concrete, navigable route string for a given id.
 *
 * ID arguments use [NEW_ID] (`0L`) as a "not applicable / create new" sentinel
 * rather than nullable query params, since Room's autoGenerate ids start at 1.
 */
object Routes {
    // Auth graph — Splash/CreateCredential/Login (no bottom nav).
    const val AUTH_GRAPH = "auth_graph"
    const val SPLASH = "splash"
    const val CREATE_CREDENTIAL = "create_credential"
    const val LOGIN = "login"

    /**
     * Single entry point into the post-login app; internally hosts its own
     * NavHost + bottom nav bar (see MainScreen.kt) with 3 nested tab graphs.
     */
    const val MAIN = "main"

    // Dashboard tab graph
    const val DASHBOARD_GRAPH = "dashboard_graph"
    const val DASHBOARD = "dashboard"
    const val CATEGORY_FORM_PATTERN = "category_form/{categoryId}"
    const val LESSONS_LIST_PATTERN = "lessons_list/{categoryId}"
    const val LESSON_FORM_PATTERN = "lesson_form/{categoryId}/{lessonId}"
    const val LESSON_INFO_PATTERN = "lesson_info/{lessonId}"
    const val LESSON_ATTENDANCE_PATTERN = "lesson_attendance/{lessonId}"
    const val STUDENT_FORM_PATTERN = "student_form/{studentId}"
    const val STUDENT_DETAIL_PATTERN = "student_detail/{studentId}"
    const val STUDENT_ENROLLMENT_PATTERN = "student_enrollment/{categoryId}"
    const val SEARCH = "search"

    // Statistics tab graph
    const val STATISTICS_GRAPH = "statistics_graph"
    const val STATISTICS = "statistics"

    // Settings tab graph
    const val SETTINGS_GRAPH = "settings_graph"
    const val SETTINGS = "settings"
    const val EXPORT = "export"
    const val BACKUP_RESTORE = "backup_restore"

    /** Sentinel meaning "create new" wherever an id argument is expected. */
    const val NEW_ID = 0L

    fun categoryForm(categoryId: Long = NEW_ID) = "category_form/$categoryId"
    fun lessonsList(categoryId: Long) = "lessons_list/$categoryId"
    fun lessonForm(categoryId: Long, lessonId: Long = NEW_ID) = "lesson_form/$categoryId/$lessonId"
    fun lessonInfo(lessonId: Long) = "lesson_info/$lessonId"
    fun lessonAttendance(lessonId: Long) = "lesson_attendance/$lessonId"
    fun studentForm(studentId: Long = NEW_ID) = "student_form/$studentId"
    fun studentDetail(studentId: Long) = "student_detail/$studentId"
    fun studentEnrollment(categoryId: Long) = "student_enrollment/$categoryId"

}
