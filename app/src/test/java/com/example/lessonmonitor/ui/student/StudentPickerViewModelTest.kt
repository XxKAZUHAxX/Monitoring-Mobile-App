package com.example.lessonmonitor.ui.student

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.EnrollmentEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.EnrollmentRepository
import com.example.lessonmonitor.domain.repository.RosterEntry
import com.example.lessonmonitor.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StudentPickerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentRepository: StudentRepository = mockk()
    private val enrollmentRepository: EnrollmentRepository = mockk()

    private val ana = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)
    private val bo = StudentEntity(id = 2L, name = "Bo", createdAt = 1L, updatedAt = 1L)
    private val anaEnrollment = EnrollmentEntity(id = 10L, lessonId = 5L, studentId = 1L, enrolledAt = 1L)

    private fun buildViewModel(): StudentPickerViewModel {
        every { studentRepository.getAll() } returns flowOf(listOf(ana, bo))
        every { enrollmentRepository.getRosterForLesson(5L) } returns
            flowOf(listOf(RosterEntry(anaEnrollment, ana)))
        return StudentPickerViewModel(studentRepository, enrollmentRepository)
    }

    @Test
    fun `load marks already-enrolled students and leaves others unmarked`() {
        val viewModel = buildViewModel()

        viewModel.load(5L)

        val rows = viewModel.uiState.value.students.associateBy { it.student.id }
        assertEquals(true, rows[1L]?.enrolled)
        assertEquals(false, rows[2L]?.enrolled)
    }

    @Test
    fun `toggleEnrollment enrolls a student who isn't on the roster yet`() {
        coEvery { enrollmentRepository.enroll(5L, 2L) } returns Unit
        val viewModel = buildViewModel()
        viewModel.load(5L)
        val boRow = viewModel.uiState.value.students.first { it.student.id == 2L }

        viewModel.toggleEnrollment(boRow)

        coVerify { enrollmentRepository.enroll(5L, 2L) }
    }

    @Test
    fun `toggleEnrollment unenrolls a student who is already on the roster`() {
        coEvery { enrollmentRepository.unenroll(anaEnrollment) } returns Unit
        val viewModel = buildViewModel()
        viewModel.load(5L)
        val anaRow = viewModel.uiState.value.students.first { it.student.id == 1L }

        viewModel.toggleEnrollment(anaRow)

        coVerify { enrollmentRepository.unenroll(anaEnrollment) }
    }

    @Test
    fun `quickAddAndEnroll fails validation when name is blank`() {
        val viewModel = buildViewModel()
        viewModel.load(5L)

        viewModel.quickAddAndEnroll()

        assertEquals("Name is required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `quickAddAndEnroll creates the student and enrolls them`() {
        coEvery { studentRepository.create("Cy") } returns 3L
        coEvery { enrollmentRepository.enroll(5L, 3L) } returns Unit
        val viewModel = buildViewModel()
        viewModel.load(5L)
        viewModel.onQuickAddNameChange("Cy")

        viewModel.quickAddAndEnroll()

        coVerify { studentRepository.create("Cy") }
        coVerify { enrollmentRepository.enroll(5L, 3L) }
        assertEquals("", viewModel.uiState.value.quickAddName)
    }

    @Test
    fun `onFilterChange narrows the roster to enrolled or not-enrolled without a new query`() {
        val viewModel = buildViewModel()
        viewModel.load(5L)

        viewModel.onFilterChange(StudentPickerViewModel.RosterFilter.ENROLLED)
        assertEquals(listOf(1L), viewModel.uiState.value.students.map { it.student.id })

        viewModel.onFilterChange(StudentPickerViewModel.RosterFilter.NOT_ENROLLED)
        assertEquals(listOf(2L), viewModel.uiState.value.students.map { it.student.id })

        viewModel.onFilterChange(StudentPickerViewModel.RosterFilter.ALL)
        assertEquals(listOf(1L, 2L), viewModel.uiState.value.students.map { it.student.id })
    }
}
