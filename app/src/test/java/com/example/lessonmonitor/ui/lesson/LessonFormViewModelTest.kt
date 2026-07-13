package com.example.lessonmonitor.ui.lesson

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.RecurrenceType
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.navigation.Routes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LessonFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonRepository: LessonRepository = mockk()

    @Test
    fun `submit fails validation when title is blank`() {
        val viewModel = LessonFormViewModel(lessonRepository)
        viewModel.load(categoryId = 3L, lessonId = Routes.NEW_ID)

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(false, onSavedCalled)
        assertEquals("Title is required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit fails validation when recurring weekly with no days selected`() {
        val viewModel = LessonFormViewModel(lessonRepository)
        viewModel.load(categoryId = 3L, lessonId = Routes.NEW_ID)
        viewModel.onTitleChange("Algebra")
        viewModel.onRecurringChange(true)
        viewModel.onRecurrenceTypeChange(RecurrenceType.WEEKLY)

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(false, onSavedCalled)
        assertEquals("Select at least one day of the week", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit fails validation when start date is malformed`() {
        val viewModel = LessonFormViewModel(lessonRepository)
        viewModel.load(categoryId = 3L, lessonId = Routes.NEW_ID)
        viewModel.onTitleChange("Algebra")
        viewModel.onStartDateTextChange("not-a-date")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(false, onSavedCalled)
        assertEquals("Start date must be in yyyy-MM-dd format", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit creates a non-recurring lesson with the given fields`() {
        coEvery {
            lessonRepository.create(
                categoryId = 3L,
                title = "Algebra",
                description = null,
                facilitatorName = null,
                place = null,
                isRecurring = false,
                recurrenceType = RecurrenceType.NONE,
                recurrenceDaysOfWeek = null,
                startDate = any(),
                endDate = null,
                startTime = null,
                endTime = null
            )
        } returns 1L
        val viewModel = LessonFormViewModel(lessonRepository)
        viewModel.load(categoryId = 3L, lessonId = Routes.NEW_ID)
        viewModel.onTitleChange("Algebra")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit creates a recurring weekly lesson with the selected days encoded as CSV`() {
        val slotHolder = mutableListOf<String?>()
        coEvery {
            lessonRepository.create(
                categoryId = 3L,
                title = "Algebra",
                description = null,
                facilitatorName = null,
                place = null,
                isRecurring = true,
                recurrenceType = RecurrenceType.WEEKLY,
                recurrenceDaysOfWeek = "1,3",
                startDate = any(),
                endDate = null,
                startTime = null,
                endTime = null
            )
        } returns 1L
        val viewModel = LessonFormViewModel(lessonRepository)
        viewModel.load(categoryId = 3L, lessonId = Routes.NEW_ID)
        viewModel.onTitleChange("Algebra")
        viewModel.onRecurringChange(true)
        viewModel.onRecurrenceTypeChange(RecurrenceType.WEEKLY)
        viewModel.onToggleDayOfWeek(1)
        viewModel.onToggleDayOfWeek(3)

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        coVerify {
            lessonRepository.create(
                categoryId = 3L,
                title = "Algebra",
                description = null,
                facilitatorName = null,
                place = null,
                isRecurring = true,
                recurrenceType = RecurrenceType.WEEKLY,
                recurrenceDaysOfWeek = "1,3",
                startDate = any(),
                endDate = null,
                startTime = null,
                endTime = null
            )
        }
    }

    @Test
    fun `load populates fields from an existing lesson including days of week`() {
        val existing = LessonEntity(
            id = 9L,
            categoryId = 3L,
            title = "Algebra",
            isRecurring = true,
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceDaysOfWeek = "1,3",
            startDate = 19000L,
            startTime = 540,
            endTime = 600,
            createdAt = 1L,
            updatedAt = 1L
        )
        coEvery { lessonRepository.getById(9L) } returns flowOf(existing)
        val viewModel = LessonFormViewModel(lessonRepository)

        viewModel.load(categoryId = 3L, lessonId = 9L)

        assertEquals("Algebra", viewModel.uiState.value.title)
        assertEquals(setOf(1, 3), viewModel.uiState.value.recurrenceDaysOfWeek)
        assertEquals("09:00", viewModel.uiState.value.startTimeText)
        assertEquals("10:00", viewModel.uiState.value.endTimeText)
    }
}
