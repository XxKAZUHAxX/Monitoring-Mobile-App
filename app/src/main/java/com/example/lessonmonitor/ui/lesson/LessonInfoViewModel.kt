package com.example.lessonmonitor.ui.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class LessonInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val lessonId: Long = savedStateHandle.get<Long>("lessonId") ?: 0L

    data class UiState(
        val lesson: LessonEntity? = null,
        val lessonTitle: String = "",
        val lessonDate: String = "",
        val lessonTime: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var loadedLessonId: Long? = null

    fun load() {
        if (lessonId == loadedLessonId) return
        loadedLessonId = lessonId

        viewModelScope.launch {
            val lesson = lessonRepository.getById(lessonId).first() ?: return@launch
            _uiState.update {
                it.copy(
                    lesson = lesson,
                    lessonTitle = lesson.title,
                    lessonDate = LocalDate.ofEpochDay(lesson.startDate).toString(),
                    lessonTime = lesson.startTime?.let { minutes ->
                        LocalTime.of(minutes / 60, minutes % 60).toString()
                    }.orEmpty()
                )
            }
        }
    }
}
