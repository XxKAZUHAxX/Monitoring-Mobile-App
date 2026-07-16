package com.example.lessonmonitor.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.worker.LessonAlarmScheduler
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

@HiltViewModel
class LessonFormViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val lessonAlarmScheduler: LessonAlarmScheduler
) : ViewModel() {

    data class UiState(
        val lessonId: Long = Routes.NEW_ID,
        val title: String = "",
        val description: String = "",
        val facilitatorName: String = "",
        val place: String = "",
        val startDateText: String = LocalDate.now().toString(),
        val startTimeText: String = "",
        val errorMessage: String? = null,
        val isSubmitting: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var targetCategoryId: Long = Routes.NEW_ID
    private var loadedLessonId: Long? = null
    private var loadedLesson: LessonEntity? = null

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
                startDateText = LocalDate.ofEpochDay(lesson.startDate).toString(),
                startTimeText = lesson.startTime?.let { formatMinutes(it) }.orEmpty()
            )
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onFacilitatorNameChange(value: String) = _uiState.update { it.copy(facilitatorName = value) }
    fun onPlaceChange(value: String) = _uiState.update { it.copy(place = value) }
    fun onStartDateTextChange(value: String) = _uiState.update { it.copy(startDateText = value, errorMessage = null) }
    fun onStartTimeTextChange(value: String) = _uiState.update { it.copy(startTimeText = value, errorMessage = null) }

    fun submit(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title is required") }
            return
        }

        val startDate = try {
            LocalDate.parse(state.startDateText.trim())
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "Start date must be in yyyy-MM-dd format") }
            return
        }

        val startTime = try {
            state.startTimeText.trim().takeIf { it.isNotBlank() }?.let(LocalTime::parse)
        } catch (e: DateTimeParseException) {
            _uiState.update { it.copy(errorMessage = "Start time must be in HH:mm format") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val title = state.title.trim()
            val description = state.description.ifBlank { null }
            val facilitatorName = state.facilitatorName.ifBlank { null }
            val place = state.place.ifBlank { null }
            val startDateEpoch = startDate.toEpochDay()
            val startTimeMinutes = startTime?.let { it.hour * 60 + it.minute }

            if (state.lessonId == Routes.NEW_ID) {
                val lessonId = lessonRepository.create(
                    categoryId = targetCategoryId,
                    title = title,
                    description = description,
                    facilitatorName = facilitatorName,
                    place = place,
                    startDate = startDateEpoch,
                    startTime = startTimeMinutes
                )
                // Schedule alarms if start time is set
                if (startTimeMinutes != null) {
                    lessonAlarmScheduler.scheduleForLesson(lessonId, title, startDateEpoch, startTimeMinutes)
                }
            } else {
                val base = loadedLesson
                if (base != null) {
                    val oldStartTime = base.startTime
                    val oldStartDate = base.startDate

                    lessonRepository.update(
                        base.copy(
                            title = title,
                            description = description,
                            facilitatorName = facilitatorName,
                            place = place,
                            startDate = startDateEpoch,
                            startTime = startTimeMinutes
                        )
                    )

                    // Reschedule alarms if date/time changed
                    if (startDateEpoch != oldStartDate || startTimeMinutes != oldStartTime) {
                        lessonAlarmScheduler.cancelForLesson(base.id)
                        if (startTimeMinutes != null) {
                            lessonAlarmScheduler.scheduleForLesson(base.id, title, startDateEpoch, startTimeMinutes)
                        }
                    }
                }
            }
            _uiState.update { it.copy(isSubmitting = false) }
            onSaved()
        }
    }

    private fun formatMinutes(minutes: Int): String = LocalTime.of(minutes / 60, minutes % 60).toString()
}
