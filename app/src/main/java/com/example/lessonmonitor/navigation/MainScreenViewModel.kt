package com.example.lessonmonitor.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.domain.schedule.RecurringSessionGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trigger point for the recurring-session rolling window (PLAN.md §1
 * assumption #4 / milestone #9): [MainScreen] is the single composable the
 * user always passes through once logged in (i.e. "the app opens"), so
 * regenerating here — once per [MainScreenViewModel] instance, i.e. once per
 * app-open — satisfies that trigger without needing a Calendar screen yet.
 * `RecurringSessionGenerator.generateUpcomingSessions` is idempotent, so
 * re-running it on process restarts/config changes is always safe.
 */
@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val recurringSessionGenerator: RecurringSessionGenerator
) : ViewModel() {

    init {
        viewModelScope.launch {
            recurringSessionGenerator.generateUpcomingSessions()
        }
    }
}
