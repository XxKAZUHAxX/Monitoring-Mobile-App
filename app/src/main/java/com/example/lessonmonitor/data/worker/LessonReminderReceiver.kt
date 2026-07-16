package com.example.lessonmonitor.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives AlarmManager broadcasts and posts a local notification via
 * [NotificationHelper.showLessonReminder]. Registered in AndroidManifest.xml
 * with `exported=false` — only the local AlarmManager sends these.
 */
class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_LESSON_REMINDER) return

        val lessonId = intent.getLongExtra(NotificationHelper.EXTRA_LESSON_ID, -1L)
        val lessonTitle = intent.getStringExtra(NotificationHelper.EXTRA_LESSON_TITLE) ?: return
        val isAdvanceNotice = intent.getBooleanExtra(NotificationHelper.EXTRA_ADVANCE_NOTICE, false)

        if (lessonId == -1L) return

        NotificationHelper.showLessonReminder(context, lessonId, lessonTitle, isAdvanceNotice)
    }
}
