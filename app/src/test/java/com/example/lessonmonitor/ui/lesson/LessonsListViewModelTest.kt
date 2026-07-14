package com.example.lessonmonitor.ui.lesson

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.domain.repository.LessonDeleteImpact
import com.example.lessonmonitor.domain.repository.LessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LessonsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonRepository: LessonRepository = mockk()

    private val sampleLesson = LessonEntity(id = 1L, categoryId = 3L, title = "Algebra", startDate = 19000L, createdAt = 1L, updatedAt = 1L)

    @Test
    fun `load subscribes to lessons for the given category`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(listOf(sampleLesson))
        val viewModel = LessonsListViewModel(lessonRepository)

        viewModel.load(3L)

        assertEquals(listOf(sampleLesson), viewModel.uiState.value.lessons)
    }

    @Test
    fun `load is a no-op when called again with the same categoryId`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(listOf(sampleLesson))
        val viewModel = LessonsListViewModel(lessonRepository)

        viewModel.load(3L)
        viewModel.load(3L)

        coVerify(exactly = 1) { lessonRepository.getAllByCategory(3L) }
    }

    @Test
    fun `requestDelete fetches the impact and populates pendingDelete`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(emptyList())
        val impact = LessonDeleteImpact(enrollmentCount = 20, sessionCount = 8, recordCount = 160)
        coEvery { lessonRepository.getDeleteImpact(1L) } returns impact
        val viewModel = LessonsListViewModel(lessonRepository)
        viewModel.load(3L)

        viewModel.requestDelete(sampleLesson)

        assertEquals(sampleLesson, viewModel.uiState.value.pendingDelete?.lesson)
        assertEquals(impact, viewModel.uiState.value.pendingDelete?.impact)
    }

    @Test
    fun `confirmDelete deletes the pending lesson and clears pendingDelete`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(emptyList())
        val impact = LessonDeleteImpact(enrollmentCount = 0, sessionCount = 0, recordCount = 0)
        coEvery { lessonRepository.getDeleteImpact(1L) } returns impact
        coEvery { lessonRepository.delete(sampleLesson) } returns Unit
        val viewModel = LessonsListViewModel(lessonRepository)
        viewModel.load(3L)
        viewModel.requestDelete(sampleLesson)

        viewModel.confirmDelete()

        coVerify { lessonRepository.delete(sampleLesson) }
        assertNull(viewModel.uiState.value.pendingDelete)
    }

    @Test
    fun `onFilterChange narrows the lesson list to recurring or one-off without a new query`() {
        val recurringLesson = sampleLesson.copy(id = 2L, isRecurring = true)
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(listOf(sampleLesson, recurringLesson))
        val viewModel = LessonsListViewModel(lessonRepository)
        viewModel.load(3L)

        viewModel.onFilterChange(LessonsListViewModel.LessonFilter.RECURRING)
        assertEquals(listOf(recurringLesson), viewModel.uiState.value.lessons)

        viewModel.onFilterChange(LessonsListViewModel.LessonFilter.ONE_OFF)
        assertEquals(listOf(sampleLesson), viewModel.uiState.value.lessons)

        viewModel.onFilterChange(LessonsListViewModel.LessonFilter.ALL)
        assertEquals(listOf(sampleLesson, recurringLesson), viewModel.uiState.value.lessons)

        coVerify(exactly = 1) { lessonRepository.getAllByCategory(3L) }
    }
}
