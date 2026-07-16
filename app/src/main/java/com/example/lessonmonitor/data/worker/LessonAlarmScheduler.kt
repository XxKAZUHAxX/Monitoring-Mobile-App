package com.example.lessonmonitor.data.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
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
 *
 * Alarm scheduling failures are caught and logged rather than propagated —
 * a lesson save should never fail because an alarm couldn't be set.
 */
@Singleton
class LessonAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lessonRepository: LessonRepository
) {

    companion object {
        private const val TAG = "LessonAlarmScheduler"
        private const val MILLIS_PER_MINUTE = 60_000L
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val ADVANCE_MINUTES = 10L
    }

    /**
     * Schedules both alarms for a lesson. Skips any alarm whose trigger time is
     * already in the past. Failures are caught and logged.
     */
    fun scheduleForLesson(lessonId: Long, lessonTitle: String, startDate: Long, startTime: Int) {
        try {
            val triggerMillis = startDate * MILLIS_PER_DAY + startTime.toLong() * MILLIS_PER_MINUTE
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for lesson $lessonId", e)
        }
    }

    /** Cancels both alarms for a lesson. Failures are caught and logged. */
    fun cancelForLesson(lessonId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val advanceCode = (lessonId * 2).toInt()
            val atStartCode = (lessonId * 2 + 1).toInt()
            listOf(advanceCode, atStartCode).forEach { requestCode ->
                val intent = Intent(context, LessonReminderReceiver::class.java).apply {
                    action = NotificationHelper.ACTION_LESSON_REMINDER
                }
                val pi = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pi?.let {
                    alarmManager.cancel(it)
                    it.cancel()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm for lesson $lessonId", e)
        }
    }

    /** Reschedules alarms for all future lessons that have a start time. Called on boot and app update. */
    suspend fun rescheduleAll() {
        try {
            val lessons = lessonRepository.getAll().first()
            for (lesson in lessons) {
                val startTime = lesson.startTime ?: continue
                scheduleForLesson(lesson.id, lesson.title, lesson.startDate, startTime)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reschedule all alarms", e)
        }
    }

    private fun scheduleAlarm(lessonId: Long, lessonTitle: String, triggerMillis: Long, isAdvanceNotice: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val requestCode = if (isAdvanceNotice) (lessonId * 2).toInt() else (lessonId * 2 + 1).toInt()

        val intent = Intent(context, LessonReminderReceiver::class.java).apply {
            action = NotificationHelper.ACTION_LESSON_REMINDER
            putExtra(NotificationHelper.EXTRA_LESSON_ID, lessonId)
            putExtra(NotificationHelper.EXTRA_LESSON_TITLE, lessonTitle)
            putExtra(NotificationHelper.EXTRA_ADVANCE_NOTICE, isAdvanceNotice)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }
}
