package com.example.lessonmonitor.domain.schedule

import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Feature: Recurring/Scheduled Lessons (PLAN.md §7 milestone 9).
 *
 * Reads every recurring [LessonEntity] (`isRecurring = true`) and, for each
 * occurrence date its [LessonEntity.recurrenceType]/[LessonEntity.recurrenceDaysOfWeek]
 * rule produces inside a rolling lookahead window, calls
 * [AttendanceRepository.createSession] — which is idempotent on the unique
 * (lessonId, sessionDate) index, so calling [generateUpcomingSessions]
 * repeatedly (e.g. every app open) never creates duplicates.
 *
 * Per PLAN.md §1 assumption #4, the window is only ever generated going
 * forward from "today" — past occurrence dates before [today] are never
 * (re)created here, since that's the job of the one-off manual session
 * creation already covered in milestone #7.
 */
@Singleton
class RecurringSessionGenerator @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val attendanceRepository: AttendanceRepository
) {

    suspend fun generateUpcomingSessions(
        today: LocalDate = LocalDate.now(),
        windowDays: Long = DEFAULT_WINDOW_DAYS
    ) {
        val todayEpochDay = today.toEpochDay()
        val windowEndEpochDay = todayEpochDay + windowDays
        lessonRepository.getAllRecurring().forEach { lesson ->
            generateForLesson(lesson, todayEpochDay, windowEndEpochDay)
        }
    }

    private suspend fun generateForLesson(lesson: LessonEntity, todayEpochDay: Long, windowEndEpochDay: Long) {
        val rangeStart = maxOf(lesson.startDate, todayEpochDay)
        val rangeEnd = lesson.endDate?.let { minOf(it, windowEndEpochDay) } ?: windowEndEpochDay
        if (rangeStart > rangeEnd) return

        val daysOfWeek = parseDaysOfWeek(lesson.recurrenceDaysOfWeek)
        for (epochDay in rangeStart..rangeEnd) {
            if (occursOn(lesson.recurrenceType, epochDay, daysOfWeek)) {
                attendanceRepository.createSession(lesson.id, epochDay)
            }
        }
    }

    private fun occursOn(recurrenceType: RecurrenceType, epochDay: Long, daysOfWeek: Set<Int>): Boolean =
        when (recurrenceType) {
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKLY, RecurrenceType.CUSTOM_DAYS ->
                LocalDate.ofEpochDay(epochDay).dayOfWeek.value in daysOfWeek
            RecurrenceType.NONE -> false
        }

    /** [csv] is ISO-8601 day-of-week ints (1=Monday..7=Sunday) — see [LessonEntity.recurrenceDaysOfWeek]. */
    private fun parseDaysOfWeek(csv: String?): Set<Int> =
        csv?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.toSet()
            ?: emptySet()

    companion object {
        const val DEFAULT_WINDOW_DAYS = 60L
    }
}
