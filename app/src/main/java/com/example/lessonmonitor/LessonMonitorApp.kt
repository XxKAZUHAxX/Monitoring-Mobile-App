package com.example.lessonmonitor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.lessonmonitor.data.worker.NotificationHelper
import com.example.lessonmonitor.data.worker.SessionWindowWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Application entry point, annotated so Hilt's DI graph is available app-wide.
 *
 * Implements [Configuration.Provider] so [androidx.hilt.work.HiltWorker]-annotated
 * [androidx.work.Worker] classes (e.g. [SessionWindowWorker]) are created by
 * the Hilt-aware [HiltWorkerFactory].
 *
 * Notification channel is created here once on cold start (idempotent — calling
 * `createNotificationChannel` again is harmless).
 */
@HiltAndroidApp
class LessonMonitorApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        enqueueHousekeepingWorker()
    }

    private fun enqueueHousekeepingWorker() {
        val dailyRequest = PeriodicWorkRequestBuilder<SessionWindowWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "session_window_housekeeping",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }
}
