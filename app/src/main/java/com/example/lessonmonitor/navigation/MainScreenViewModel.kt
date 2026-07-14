package com.example.lessonmonitor.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.worker.AlarmScheduler
import com.example.lessonmonitor.domain.schedule.RecurringSessionGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trigger point for the recurring-session rolling window + alarm scheduling
 * (PLAN.md §1 assumption #4 / milestone #9 / milestone #14): [MainScreen] is
 * the single composable the user always passes through once logged in (i.e.
 * "the app opens"), so regenerating + rescheduling here — once per
 * [MainScreenViewModel] instance, i.e. once per app-open — satisfies that
 * trigger. Both `generateUpcomingSessions` and `syncAlarms` are idempotent,
 * so re-running them on process restarts/config changes is always safe.
 */
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val recurringSessionGenerator: RecurringSessionGenerator,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    init {
        viewModelScope.launch {
            recurringSessionGenerator.generateUpcomingSessions()
            alarmScheduler.syncAlarms()
        }
    }
}
