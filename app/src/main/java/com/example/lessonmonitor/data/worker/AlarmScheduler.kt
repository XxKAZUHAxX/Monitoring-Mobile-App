package com.example.lessonmonitor.data.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels [AlarmManager] alarms for upcoming
 * [AttendanceSessionEntity] rows in the 60-day rolling lookahead window.
 *
 * Called from [SessionWindowWorker] (daily housekeeping) and from
 * [MainScreenViewModel] (app-open trigger), both after
 * [RecurringSessionGenerator.generateUpcomingSessions] has already refreshed
 * the session window — so the sessions this queries are always up-to-date.
 *
 * Each alarm fires a broadcast to [SessionReminderReceiver], which shows a
 * notification with a deep link into the Attendance Session screen.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attendanceRepository: AttendanceRepository,
    private val lessonRepository: LessonRepository
) {

    /**
     * One-shot: reads every [AttendanceSessionEntity] whose date is in
     * `[today, today + windowDays]`, joins each with its [LessonEntity] to
     * get the `startTime`, and schedules an exact alarm for any session
     * that has a known start time.
     *
     * Calling this repeatedly is safe — [PendingIntent.FLAG_UPDATE_CURRENT]
     * means re-scheduling the same session id overwrites the old alarm rather
     * than creating a duplicate.
     */
    suspend fun syncAlarms(today: LocalDate = LocalDate.now(), windowDays: Long = 60) {
        val todayEpochDay = today.toEpochDay()
        val windowEndEpochDay = todayEpochDay + windowDays

        val sessions = attendanceRepository.getSessionsInRange(todayEpochDay, windowEndEpochDay).first()
        val lessonsById: Map<Long, LessonEntity> = lessonRepository.getAll().first().associateBy { it.id }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (entry in sessions) {
            val lesson = lessonsById[entry.session.lessonId] ?: continue
            val startTimeMinutes = lesson.startTime ?: continue // no time → no alarm

            val triggerAtMillis = entry.session.sessionDate * MILLIS_PER_DAY + startTimeMinutes * MILLIS_PER_MINUTE

            // Don't schedule alarms that would fire in the past.
            val nowMillis = System.currentTimeMillis()
            if (triggerAtMillis <= nowMillis) continue

            val intent = Intent(context, SessionReminderReceiver::class.java).apply {
                action = NotificationHelper.ACTION_SESSION_REMINDER
                putExtra(NotificationHelper.EXTRA_LESSON_ID, lesson.id)
                putExtra(NotificationHelper.EXTRA_SESSION_ID, entry.session.id)
                putExtra(NotificationHelper.EXTRA_LESSON_TITLE, entry.lessonTitle)
                putExtra(NotificationHelper.EXTRA_SESSION_DATE, entry.session.sessionDate)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                entry.session.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    companion object {
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val MILLIS_PER_DAY = 86_400_000L
    }
}
