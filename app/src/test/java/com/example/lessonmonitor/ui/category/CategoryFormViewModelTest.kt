package com.example.lessonmonitor.ui.category

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.navigation.Routes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CategoryFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository: CategoryRepository = mockk()

    @Test
    fun `load with NEW_ID resets to a blank form`() {
        val viewModel = CategoryFormViewModel(categoryRepository)

        viewModel.load(Routes.NEW_ID)

        assertEquals("", viewModel.uiState.value.name)
        assertEquals(Routes.NEW_ID, viewModel.uiState.value.categoryId)
    }

    @Test
    fun `load with an existing id populates the form from the repository`() {
        val existing = CategoryEntity(id = 5L, name = "Math", description = "desc", color = 1, icon = "📘", createdAt = 1L, updatedAt = 1L)
        coEvery { categoryRepository.getById(5L) } returns flowOf(existing)
        val viewModel = CategoryFormViewModel(categoryRepository)

        viewModel.load(5L)

        assertEquals("Math", viewModel.uiState.value.name)
        assertEquals("desc", viewModel.uiState.value.description)
        assertEquals(1, viewModel.uiState.value.color)
        assertEquals("📘", viewModel.uiState.value.icon)
    }

    @Test
    fun `submit fails validation when name is blank`() {
        val viewModel = CategoryFormViewModel(categoryRepository)
        viewModel.load(Routes.NEW_ID)

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(false, onSavedCalled)
        assertEquals("Name is required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit creates a new category when categoryId is NEW_ID`() {
        coEvery { categoryRepository.create("Math", null, null, null) } returns 1L
        val viewModel = CategoryFormViewModel(categoryRepository)
        viewModel.load(Routes.NEW_ID)
        viewModel.onNameChange("Math")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        coVerify { categoryRepository.create("Math", null, null, null) }
    }

    @Test
    fun `submit updates the existing category preserving createdAt`() {
        val existing = CategoryEntity(id = 5L, name = "Old", createdAt = 100L, updatedAt = 100L)
        coEvery { categoryRepository.getById(5L) } returns flowOf(existing)
        coEvery { categoryRepository.update(any()) } returns Unit
        val viewModel = CategoryFormViewModel(categoryRepository)
        viewModel.load(5L)
        viewModel.onNameChange("New")

        var onSavedCalled = false
        viewModel.submit { onSavedCalled = true }

        assertEquals(true, onSavedCalled)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify { categoryRepository.update(match { it.id == 5L && it.name == "New" && it.createdAt == 100L }) }
    }
}
