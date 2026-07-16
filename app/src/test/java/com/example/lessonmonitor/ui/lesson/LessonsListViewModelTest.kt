package com.example.lessonmonitor.ui.lesson

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.LessonDeleteImpact
import com.example.lessonmonitor.domain.repository.LessonRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LessonsListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val lessonRepository: LessonRepository = mockk()
    private val enrollmentRepository: EnrollmentRepository = mockk()

    private val sampleLesson = LessonEntity(
        id = 1L, categoryId = 3L, title = "Algebra",
        startDate = 19000L, createdAt = 1L, updatedAt = 1L
    )

    @Test
    fun `load subscribes to lessons and students for the given category`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(listOf(sampleLesson))
        every { enrollmentRepository.getRosterForCategory(3L) } returns flowOf(emptyList())
        val viewModel = LessonsListViewModel(lessonRepository, enrollmentRepository)

        viewModel.load(3L)

        assertEquals(listOf(sampleLesson), viewModel.uiState.value.lessons)
    }

    @Test
    fun `load is a no-op when called again with the same categoryId`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(listOf(sampleLesson))
        every { enrollmentRepository.getRosterForCategory(3L) } returns flowOf(emptyList())
        val viewModel = LessonsListViewModel(lessonRepository, enrollmentRepository)

        viewModel.load(3L)
        viewModel.load(3L)

        coVerify(exactly = 1) { lessonRepository.getAllByCategory(3L) }
    }

    @Test
    fun `tab switching changes selected tab`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(emptyList())
        every { enrollmentRepository.getRosterForCategory(3L) } returns flowOf(emptyList())
        val viewModel = LessonsListViewModel(lessonRepository, enrollmentRepository)
        viewModel.load(3L)

        assertEquals(LessonsListViewModel.Tab.LESSONS, viewModel.uiState.value.selectedTab)

        viewModel.onTabSelected(LessonsListViewModel.Tab.STUDENTS)
        assertEquals(LessonsListViewModel.Tab.STUDENTS, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `requestDelete fetches the impact and populates pendingDelete`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(emptyList())
        every { enrollmentRepository.getRosterForCategory(3L) } returns flowOf(emptyList())
        val impact = LessonDeleteImpact(enrollmentCount = 0, recordCount = 160)
        coEvery { lessonRepository.getDeleteImpact(1L) } returns impact
        val viewModel = LessonsListViewModel(lessonRepository, enrollmentRepository)
        viewModel.load(3L)

        viewModel.requestDelete(sampleLesson)

        assertEquals(sampleLesson, viewModel.uiState.value.pendingDelete?.lesson)
        assertEquals(impact, viewModel.uiState.value.pendingDelete?.impact)
    }

    @Test
    fun `confirmDelete deletes the pending lesson and clears pendingDelete`() {
        coEvery { lessonRepository.getAllByCategory(3L) } returns flowOf(emptyList())
        every { enrollmentRepository.getRosterForCategory(3L) } returns flowOf(emptyList())
        val impact = LessonDeleteImpact(enrollmentCount = 0, recordCount = 0)
        coEvery { lessonRepository.getDeleteImpact(1L) } returns impact
        coEvery { lessonRepository.delete(sampleLesson) } returns Unit
        val viewModel = LessonsListViewModel(lessonRepository, enrollmentRepository)
        viewModel.load(3L)
        viewModel.requestDelete(sampleLesson)

        viewModel.confirmDelete()

        coVerify { lessonRepository.delete(sampleLesson) }
        assertNull(viewModel.uiState.value.pendingDelete)
    }
}
