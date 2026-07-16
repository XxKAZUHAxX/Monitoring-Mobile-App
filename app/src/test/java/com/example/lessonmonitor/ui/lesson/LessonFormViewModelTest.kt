package com.example.lessonmonitor.ui.lesson

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.worker.LessonAlarmScheduler
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.navigation.Routes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class LessonFormViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val lessonRepository: LessonRepository = mockk()
    private val lessonAlarmScheduler: LessonAlarmScheduler = mockk()

    @Test
    fun `submit with blank title shows error`() = runTest {
        val vm = LessonFormViewModel(lessonRepository, lessonAlarmScheduler)
        vm.load(1L, Routes.NEW_ID)
        vm.onTitleChange("")
        vm.submit {}

        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `submit creates lesson and schedules alarm when start time is set`() = runTest {
        val vm = LessonFormViewModel(lessonRepository, lessonAlarmScheduler)
        coEvery { lessonRepository.getMaxSortOrder(1L) } returns 0
        coEvery { lessonRepository.create(any(), any(), any(), any(), any(), any(), any()) } returns 42L
        coEvery { lessonAlarmScheduler.scheduleForLesson(any(), any(), any(), any()) } returns Unit
        vm.load(1L, Routes.NEW_ID)
        vm.onTitleChange("Math")
        vm.onStartTimeTextChange("09:00")

        var saved = false
        vm.submit { saved = true }

        coVerify { lessonRepository.create(any(), "Math", any(), any(), any(), any(), any()) }
        coVerify { lessonAlarmScheduler.scheduleForLesson(42L, "Math", any(), 540) }
    }

    @Test
    fun `submit edits existing lesson and reschedules alarm`() = runTest {
        val existing = LessonEntity(
            id = 5L, categoryId = 1L, title = "Old", startDate = 19000L,
            startTime = 480, sortOrder = 0, createdAt = 1L, updatedAt = 1L
        )
        every { lessonRepository.getById(5L) } returns flowOf(existing)
        coEvery { lessonRepository.update(any()) } returns Unit
        coEvery { lessonAlarmScheduler.cancelForLesson(any()) } returns Unit
        coEvery { lessonAlarmScheduler.scheduleForLesson(any(), any(), any(), any()) } returns Unit

        val vm = LessonFormViewModel(lessonRepository, lessonAlarmScheduler)
        vm.load(1L, 5L)

        // Wait for load to complete
        while (vm.uiState.value.title.isEmpty()) { /* wait */ }

        vm.onTitleChange("New Title")
        vm.onStartTimeTextChange("10:00")
        var saved = false
        vm.submit { saved = true }

        coVerify { lessonRepository.update(any()) }
        coVerify { lessonAlarmScheduler.cancelForLesson(5L) }
        coVerify { lessonAlarmScheduler.scheduleForLesson(5L, "New Title", any(), 600) }
    }
}
