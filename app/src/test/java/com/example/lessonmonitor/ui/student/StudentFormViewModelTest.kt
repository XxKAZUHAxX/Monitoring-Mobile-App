package com.example.lessonmonitor.ui.student

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.StudentRepository
import com.example.lessonmonitor.navigation.Routes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class StudentFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val studentRepository: StudentRepository = mockk()

    @Test
    fun `load with NEW_ID resets to a blank form`() {
        val viewModel = StudentFormViewModel(studentRepository)

        viewModel.load(Routes.NEW_ID)

        assertEquals("", viewModel.uiState.value.name)
        assertEquals(Routes.NEW_ID, viewModel.uiState.value.studentId)
    }

    @Test
    fun `load with an existing id populates the form from the repository`() {
        val existing = StudentEntity(id = 5L, name = "Ana", phone = "555", email = "ana@example.com", notes = "note", photoPath = "/p.jpg", createdAt = 1L, updatedAt = 1L)
        coEvery { studentRepository.getById(5L) } returns flowOf(existing)
        val viewModel = StudentFormViewModel(studentRepository)

        viewModel.load(5L)

        assertEquals("Ana", viewModel.uiState.value.name)
        assertEquals("555", viewModel.uiState.value.phone)
        assertEquals("ana@example.com", viewModel.uiState.value.email)
        assertEquals("note", viewModel.uiState.value.notes)
        assertEquals("/p.jpg", viewModel.uiState.value.photoPath)
    }

    @Test
    fun `submit fails validation when name is blank`() {
        val viewModel = StudentFormViewModel(studentRepository)
        viewModel.load(Routes.NEW_ID)

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(false, onSavedCalled)
        assertEquals("Name is required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit creates a new student when studentId is NEW_ID`() {
        coEvery { studentRepository.create("Ana", null, null, null, null) } returns 1L
        val viewModel = StudentFormViewModel(studentRepository)
        viewModel.load(Routes.NEW_ID)
        viewModel.onNameChange("Ana")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        coVerify { studentRepository.create("Ana", null, null, null, null) }
    }

    @Test
    fun `submit updates the existing student preserving createdAt`() {
        val existing = StudentEntity(id = 5L, name = "Old", createdAt = 100L, updatedAt = 100L)
        coEvery { studentRepository.getById(5L) } returns flowOf(existing)
        coEvery { studentRepository.update(any()) } returns Unit
        val viewModel = StudentFormViewModel(studentRepository)
        viewModel.load(5L)
        viewModel.onNameChange("New")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify { studentRepository.update(match { it.id == 5L && it.name == "New" && it.createdAt == 100L }) }
    }
}
