package com.example.lessonmonitor

import android.app.Application
import com.example.lessonmonitor.data.worker.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point, annotated so Hilt's DI graph is available app-wide.
 *
 * Notification channel is created here once on cold start (idempotent — calling
 * `createChannel` again is harmless).
 */
@HiltAndroidApp
class LessonMonitorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
