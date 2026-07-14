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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Plain Kotlin `object` — no DI. The notification channel is created idempotently
 * from [LessonMonitorApp.onCreate] (and re-created by any caller, since
 * `createNotificationChannel` is itself idempotent).
 *
 * [showSessionReminder] is called by [SessionReminderReceiver] when an
 * `AlarmManager` alarm fires. It takes a [Context] parameter so it can be
 * called from anywhere without Hilt injection.
 *
 * Each notification gets a unique id equal to `sessionId.toInt()` so new alarms
 * for the same session silently replace the old one rather than stacking up.
 */
object NotificationHelper {

    const val CHANNEL_ID = "lesson_reminders"
    private const val CHANNEL_NAME = "Lesson Reminders"

    /** Intent action used by the alarm [PendingIntent] so the receiver can identify its own broadcasts. */
    const val ACTION_SESSION_REMINDER = "com.example.lessonmonitor.action.SESSION_REMINDER"

    /** Intent extras carried from alarm → receiver → notification. */
    const val EXTRA_LESSON_ID = "lesson_id"
    const val EXTRA_SESSION_ID = "session_id"
    const val EXTRA_LESSON_TITLE = "lesson_title"
    const val EXTRA_SESSION_DATE = "session_date"

    // ---- Channel ----

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for upcoming lesson sessions"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    // ---- Notification ----

    fun showSessionReminder(
        context: Context,
        lessonId: Long,
        sessionId: Long,
        lessonTitle: String,
        sessionDateEpochDay: Long
    ) {
        val dateLabel = LocalDate.ofEpochDay(sessionDateEpochDay)
            .format(DateTimeFormatter.ofPattern("EEE, MMM d"))

        // Deep-link PendingIntent — tapping the notification opens AttendanceSession.
        val deepLinkUri = Uri.parse("lessonmonitor://lesson/$lessonId/session/$sessionId")
        val deepLinkIntent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            setClass(context, MainActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            sessionId.toInt(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // fallback; the app's own icon would be set via manifest metadata
            .setContentTitle(lessonTitle)
            .setContentText("Session on $dateLabel — tap to mark attendance")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$lessonTitle is scheduled for $dateLabel.\nTap to open the attendance screen and mark each student's status.")
            )
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(sessionId.toInt(), notification)
    }
}
