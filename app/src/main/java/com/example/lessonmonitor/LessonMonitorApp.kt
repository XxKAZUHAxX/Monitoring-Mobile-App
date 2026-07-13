package com.example.lessonmonitor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point, annotated so Hilt's DI graph is available app-wide.
 * Kept intentionally empty for now — the Notifications milestone will make
 * this implement `Configuration.Provider` once a Hilt-aware WorkManager
 * factory is needed.
 */
@HiltAndroidApp
class LessonMonitorApp : Application()
