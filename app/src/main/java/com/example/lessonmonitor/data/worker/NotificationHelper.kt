package com.example.lessonmonitor.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lessonmonitor.MainActivity

/**
 * Plain Kotlin `object` — no DI. The notification channel is created idempotently
 * from [LessonMonitorApp.onCreate].
 *
 * [showLessonReminder] is called by [LessonReminderReceiver] when an
 * `AlarmManager` alarm fires. Each notification deep-links to the
 * `LessonAttendanceScreen` for that lesson.
 *
 * Notification IDs: advance notice uses `lessonId.toInt()`, at-start uses
 * `lessonId.toInt() + 10000`, so the at-start notification replaces the
 * advance notice rather than stacking.
 */
object NotificationHelper {

    const val CHANNEL_ID = "lesson_reminders"
    private const val CHANNEL_NAME = "Lesson Reminders"

    /** Intent action used by the alarm [PendingIntent] so the receiver can identify its own broadcasts. */
    const val ACTION_LESSON_REMINDER = "com.example.lessonmonitor.action.LESSON_REMINDER"

    /** Intent extras carried from alarm → receiver → notification. */
    const val EXTRA_LESSON_ID = "lesson_id"
    const val EXTRA_LESSON_TITLE = "lesson_title"
    const val EXTRA_ADVANCE_NOTICE = "advance_notice"

    // ---- Channel ----

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for upcoming lessons"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    // ---- Notification ----

    fun showLessonReminder(
        context: Context,
        lessonId: Long,
        lessonTitle: String,
        isAdvanceNotice: Boolean
    ) {
        val notificationId = if (isAdvanceNotice) lessonId.toInt() else lessonId.toInt() + 10000

        // Deep-link PendingIntent — tapping the notification opens LessonAttendance.
        val deepLinkUri = Uri.parse("lessonmonitor://lesson/$lessonId/attendance")
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (isAdvanceNotice) {
            "$lessonTitle starts in 10 minutes"
        } else {
            "$lessonTitle is starting now"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(lessonTitle)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\nTap to open the attendance screen.")
            )
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
