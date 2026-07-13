package com.example.lessonmonitor.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

/**
 * Captures the Lesson template's own fields (title/description/facilitator/
 * place + recurrence config) per PLAN.md §7 milestone 6. Date/time are typed
 * as plain text (`yyyy-MM-dd` / `HH:mm`) rather than Material3 date/time
 * picker widgets, to keep this milestone's surface area small and reviewable
 * without a local compiler available (see APP_LOGIC.md milestone 6 notes).
 * Generating [com.example.lessonmonitor.data.local.entity.AttendanceSessionEntity]
 * rows from a recurring lesson's config is deferred to the Recurring/Scheduled
 * Lessons milestone (#9) — this screen only persists the template.
 */
@HiltViewModel
class LessonFormViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {

    data class UiState(
        val lessonId: Long = Routes.NEW_ID,
        val title: String = "",
        val description: String = "",
        val facilitatorName: String = "",
        val place: String = "",
        val isRecurring: Boolean = false,
        val recurrenceType: RecurrenceType = RecurrenceType.WEEKLY,
        val recurrenceDaysOfWeek: Set<Int> = emptySet(),
        val startDateText: String = LocalDate.now().toString(),
        val endDateText: String = "",
        val startTimeText: String = "",
        val endTimeText: String = "",
        val errorMessage: String? = null,
        val isSubmitting: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var targetCategoryId: Long = Routes.NEW_ID
    private var loadedLessonId: Long? = null
    private var loadedLesson: LessonEntity? = null

    /** Idempotent per [lessonId]; [categoryId] is always refreshed since it's needed for a new lesson's creation. */
    fun load(categoryId: Long, lessonId: Long) {
        targetCategoryId = categoryId
        if (lessonId == loadedLessonId) return
        loadedLessonId = lessonId
        if (lessonId == Routes.NEW_ID) {
            loadedLesson = null
            _uiState.value = UiState(lessonId = Routes.NEW_ID)
            return
        }
        viewModelScope.launch {
            val lesson = lessonRepository.getById(lessonId).first() ?: return@launch
            loadedLesson = lesson
            _uiState.value = UiState(
                lessonId = lessonId,
                title = lesson.title,
                description = lesson.description.orEmpty(),
                facilitatorName = lesson.facilitatorName.orEmpty(),
                place = lesson.place.orEmpty(),
                isRecurring = lesson.isRecurring,
                recurrenceType = if (lesson.recurrenceType == RecurrenceType.NONE) {
                    RecurrenceType.WEEKLY
                } else {
                    lesson.recurrenceType
                },
                recurrenceDaysOfWeek = lesson.recurrenceDaysOfWeek
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?.toSet()
                    .orEmpty(),
                startDateText = LocalDate.ofEpochDay(lesson.startDate).toString(),
                endDateText = lesson.endDate?.let(LocalDate::ofEpochDay)?.toString().orEmpty(),
                startTimeText = lesson.startTime?.let(::formatMinutes).orEmpty(),
                endTimeText = lesson.endTime?.let(::formatMinutes).orEmpty()
            )
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onFacilitatorNameChange(value: String) = _uiState.update { it.copy(facilitatorName = value) }
    fun onPlaceChange(value: String) = _uiState.update { it.copy(place = value) }
    fun onRecurringChange(value: Boolean) = _uiState.update { it.copy(isRecurring = value, errorMessage = null) }
    fun onRecurrenceTypeChange(value: RecurrenceType) =
        _uiState.update { it.copy(recurrenceType = value, errorMessage = null) }

    fun onToggleDayOfWeek(day: Int) = _uiState.update { state ->
        val days = state.recurrenceDaysOfWeek.toMutableSet()
        if (!days.add(day)) days.remove(day)
        state.copy(recurrenceDaysOfWeek = days, errorMessage = null)
    }

    fun onStartDateTextChange(value: String) = _uiState.update { it.copy(startDateText = value, errorMessage = null) }
    fun onEndDateTextChange(value: String) = _uiState.update { it.copy(endDateText = value, errorMessage = null) }
    fun onStartTimeTextChange(value: String) = _uiState.update { it.copy(startTimeText = value, errorMessage = null) }
    fun onEndTimeTextChange(value: String) = _uiState.update { it.copy(endTimeText = value, errorMessage = null) }

    fun submit(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title is required") }
            return
        }
        val recurrenceType = if (state.isRecurring) state.recurrenceType else RecurrenceType.NONE
        if (state.isRecurring && recurrenceType != RecurrenceType.DAILY && state.recurrenceDaysOfWeek.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Select at least one day of the week") }
            return
        }

        val startDate = try {
            LocalDate.parse(state.startDateText.trim())
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "Start date must be in yyyy-MM-dd format") }
            return
        }
        val endDate = try {
            state.endDateText.trim().takeIf { it.isNotBlank() }?.let(LocalDate::parse)
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "End date must be in yyyy-MM-dd format") }
            return
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            _uiState.update { it.copy(errorMessage = "End date must be on or after the start date") }
            return
        }
        val startTime = try {
            state.startTimeText.trim().takeIf { it.isNotBlank() }?.let(LocalTime::parse)
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "Start time must be in HH:mm format") }
            return
        }
        val endTime = try {
            state.endTimeText.trim().takeIf { it.isNotBlank() }?.let(LocalTime::parse)
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "End time must be in HH:mm format") }
            return
        }

        val daysOfWeekCsv = if (state.isRecurring && recurrenceType != RecurrenceType.DAILY) {
            state.recurrenceDaysOfWeek.sorted().joinToString(",")
        } else {
            null
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val title = state.title.trim()
            val description = state.description.ifBlank { null }
            val facilitatorName = state.facilitatorName.ifBlank { null }
            val place = state.place.ifBlank { null }
            val startDateEpoch = startDate.toEpochDay()
            val endDateEpoch = if (state.isRecurring) endDate?.toEpochDay() else null
            val startTimeMinutes = startTime?.let { it.hour * 60 + it.minute }
            val endTimeMinutes = endTime?.let { it.hour * 60 + it.minute }

            if (state.lessonId == Routes.NEW_ID) {
                lessonRepository.create(
                    categoryId = targetCategoryId,
                    title = title,
                    description = description,
                    facilitatorName = facilitatorName,
                    place = place,
                    isRecurring = state.isRecurring,
                    recurrenceType = recurrenceType,
                    recurrenceDaysOfWeek = daysOfWeekCsv,
                    startDate = startDateEpoch,
                    endDate = endDateEpoch,
                    startTime = startTimeMinutes,
                    endTime = endTimeMinutes
                )
            } else {
                val base = loadedLesson
                if (base != null) {
                    lessonRepository.update(
                        base.copy(
                            title = title,
                            description = description,
                            facilitatorName = facilitatorName,
                            place = place,
                            isRecurring = state.isRecurring,
                            recurrenceType = recurrenceType,
                            recurrenceDaysOfWeek = daysOfWeekCsv,
                            startDate = startDateEpoch,
                            endDate = endDateEpoch,
                            startTime = startTimeMinutes,
                            endTime = endTimeMinutes
                        )
                    )
                }
            }
            _uiState.update { it.copy(isSubmitting = false) }
            onSaved()
        }
    }

    private fun formatMinutes(minutes: Int): String = LocalTime.of(minutes / 60, minutes % 60).toString()
}
