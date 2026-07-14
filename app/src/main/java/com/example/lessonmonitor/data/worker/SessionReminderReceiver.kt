package com.example.lessonmonitor.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives [AlarmManager] broadcasts scheduled by [AlarmScheduler] and posts a
 * notification via [NotificationHelper] for the corresponding session.
 *
 * Plain [BroadcastReceiver] — no Hilt injection. [NotificationHelper] is a
 * Kotlin `object` and takes a [Context] parameter, so this receiver stays
 * simple and doesn't need DI.
 *
 * Registered in [AndroidManifest.xml] with `exported = false` since only the
 * local [AlarmManager] sends these broadcasts.
 */
class SessionReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_SESSION_REMINDER) return

        val lessonId = intent.getLongExtra(NotificationHelper.EXTRA_LESSON_ID, -1L)
        val sessionId = intent.getLongExtra(NotificationHelper.EXTRA_SESSION_ID, -1L)
        val lessonTitle = intent.getStringExtra(NotificationHelper.EXTRA_LESSON_TITLE) ?: "Lesson"
        val sessionDate = intent.getLongExtra(NotificationHelper.EXTRA_SESSION_DATE, 0L)

        if (lessonId < 0 || sessionId < 0) return

        NotificationHelper.showSessionReminder(context, lessonId, sessionId, lessonTitle, sessionDate)
    }
}
