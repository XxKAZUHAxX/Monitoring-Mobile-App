package com.example.lessonmonitor.data.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all lesson alarms after device reboot. Uses an [EntryPoint] to
 * access the Hilt DI graph since [BroadcastReceiver] cannot use constructor
 * injection directly.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            BootReceiverEntryPoint::class.java
        )
        val alarmScheduler = entryPoint.scheduler()

        CoroutineScope(Dispatchers.IO).launch {
            alarmScheduler.rescheduleAll()
        }
    }
}

/**
 * Hilt entry point for [BootReceiver] to obtain [LessonAlarmScheduler] from
 * the DI graph without constructor injection.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BootReceiverEntryPoint {
    fun scheduler(): LessonAlarmScheduler
}
