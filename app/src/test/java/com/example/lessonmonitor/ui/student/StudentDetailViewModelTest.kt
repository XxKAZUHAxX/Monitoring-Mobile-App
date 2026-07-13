package com.example.lessonmonitor.ui.student

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.AttendanceRepository
import com.example.lessonmonitor.domain.repository.StudentDeleteImpact
import com.example.lessonmonitor.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class StudentDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentRepository: StudentRepository = mockk()
    private val attendanceRepository: AttendanceRepository = mockk()

    private val student = StudentEntity(id = 1L, name = "Ana", createdAt = 1L, updatedAt = 1L)

    private fun buildViewModel(): StudentDetailViewModel {
        coEvery { studentRepository.getById(1L) } returns flowOf(student)
        coEvery { attendanceRepository.getHistoryForStudent(1L) } returns flowOf(emptyList())
        return StudentDetailViewModel(studentRepository, attendanceRepository)
    }

    @Test
    fun `load populates the student profile and history`() {
        val viewModel = buildViewModel()

        viewModel.load(1L)

        assertEquals(student, viewModel.uiState.value.student)
        assertEquals(emptyList<Any>(), viewModel.uiState.value.history)
    }

    @Test
    fun `requestDelete fetches the impact and populates pendingDelete`() {
        val impact = StudentDeleteImpact(enrollmentCount = 2, recordCount = 20)
        coEvery { studentRepository.getDeleteImpact(1L) } returns impact
        val viewModel = buildViewModel()
        viewModel.load(1L)

        viewModel.requestDelete()

        assertEquals(impact, viewModel.uiState.value.pendingDelete)
    }

    @Test
    fun `confirmDelete deletes the student and marks the state as deleted`() {
        val impact = StudentDeleteImpact(enrollmentCount = 0, recordCount = 0)
        coEvery { studentRepository.getDeleteImpact(1L) } returns impact
        coEvery { studentRepository.delete(student) } returns Unit
        val viewModel = buildViewModel()
        viewModel.load(1L)
        viewModel.requestDelete()

        viewModel.confirmDelete()

        coVerify { studentRepository.delete(student) }
        assertEquals(true, viewModel.uiState.value.deleted)
        assertNull(viewModel.uiState.value.pendingDelete)
    }

    @Test
    fun `cancelDelete clears pendingDelete without deleting`() {
        val impact = StudentDeleteImpact(enrollmentCount = 0, recordCount = 0)
        coEvery { studentRepository.getDeleteImpact(1L) } returns impact
        val viewModel = buildViewModel()
        viewModel.load(1L)
        viewModel.requestDelete()

        viewModel.cancelDelete()

        assertNull(viewModel.uiState.value.pendingDelete)
    }
}
