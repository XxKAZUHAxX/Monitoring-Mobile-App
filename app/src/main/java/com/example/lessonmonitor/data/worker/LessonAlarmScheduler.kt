package com.example.lessonmonitor.data.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.lessonmonitor.domain.repository.LessonRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules exact alarms for lessons that have both a future [startDate] and a
 * non-null [startTime]. Two alarms per lesson:
 * - 10 minutes before start (advance notice)
 * - At the start time
 *
 * Request codes are derived from [lessonId] so alarms can be individually
 * cancelled: advance = lessonId * 2, at-start = lessonId * 2 + 1.
 */
@Singleton
class LessonAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonRepository: LessonRepository
) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val ADVANCE_MINUTES = 10L
    }

    /**
     * Schedules both alarms for a lesson. Skips any alarm whose trigger time is
     * already in the past.
     */
    fun scheduleForLesson(lessonId: Long, lessonTitle: String, startDate: Long, startTime: Int) {
        val triggerMillis = startDate * MILLIS_PER_DAY + startTime * MILLIS_PER_MINUTE
        val now = System.currentTimeMillis()

        // Advance notice (10 min before)
        val advanceTrigger = triggerMillis - ADVANCE_MINUTES * MILLIS_PER_MINUTE
        if (advanceTrigger > now) {
            scheduleAlarm(lessonId, lessonTitle, advanceTrigger, isAdvanceNotice = true)
        }

        // At-start
        if (triggerMillis > now) {
            scheduleAlarm(lessonId, lessonTitle, triggerMillis, isAdvanceNotice = false)
        }
    }

    /** Cancels both alarms for a lesson. */
    fun cancelForLesson(lessonId: Long) {
        val advanceCode = (lessonId * 2).toInt()
        val atStartCode = (lessonId * 2 + 1).toInt()
        listOf(advanceCode, atStartCode).forEach { requestCode ->
            val pendingIntent = buildPendingIntent(lessonId, lessonTitle = "", isAdvanceNotice = false)
                ?.let { PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) }
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    /** Reschedules alarms for all future lessons that have a start time. Called on boot and app update. */
    suspend fun rescheduleAll() {
        val lessons = lessonRepository.getAll().first()
        for (lesson in lessons) {
            val startTime = lesson.startTime ?: continue
            scheduleForLesson(lesson.id, lesson.title, lesson.startDate, startTime)
        }
    }

    private fun scheduleAlarm(lessonId: Long, lessonTitle: String, triggerMillis: Long, isAdvanceNotice: Boolean) {
        val requestCode = if (isAdvanceNotice) (lessonId * 2).toInt() else (lessonId * 2 + 1).toInt()
        val pendingIntent = buildPendingIntent(lessonId, lessonTitle, isAdvanceNotice)
            ?.let { PendingIntent.getBroadcast(context, requestCode, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) }
            ?: return

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }

    private fun buildPendingIntent(lessonId: Long, lessonTitle: String, isAdvanceNotice: Boolean): Intent? {
        return Intent(context, LessonReminderReceiver::class.java).apply {
            action = NotificationHelper.ACTION_LESSON_REMINDER
            putExtra(NotificationHelper.EXTRA_LESSON_ID, lessonId)
            putExtra(NotificationHelper.EXTRA_LESSON_TITLE, lessonTitle)
            putExtra(NotificationHelper.EXTRA_ADVANCE_NOTICE, isAdvanceNotice)
        }
    }
}
