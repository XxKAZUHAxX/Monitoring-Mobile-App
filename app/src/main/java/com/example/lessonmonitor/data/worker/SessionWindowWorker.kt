package com.example.lessonmonitor.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.lessonmonitor.domain.schedule.RecurringSessionGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Daily housekeeping worker (PLAN.md §1 assumption #5 / milestone 14):
 * regenerates the 60-day rolling lookahead window of [AttendanceSessionEntity]
 * rows for all recurring lessons, then reschedules [AlarmManager] alarms for
 * every session in that window.
 *
 * Scheduled as a periodic [androidx.work.WorkRequest] from
 * [LessonMonitorApp.onCreate]. Both steps are safe to repeat (idempotent
 * session generation + [PendingIntent.FLAG_UPDATE_CURRENT] on alarms).
 */
@HiltWorker
class SessionWindowWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val recurringSessionGenerator: RecurringSessionGenerator,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        recurringSessionGenerator.generateUpcomingSessions()
        alarmScheduler.syncAlarms()
        Result.success()
    } catch (e: Exception) {
        // Transient failure (e.g. Room is temporarily closed) → retry later.
        Result.retry()
    }
}
