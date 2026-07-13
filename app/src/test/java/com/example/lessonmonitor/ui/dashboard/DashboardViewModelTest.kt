package com.example.lessonmonitor.ui.dashboard

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.domain.repository.CategoryDeleteImpact
import com.example.lessonmonitor.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository: CategoryRepository = mockk()

    private val sampleCategory = CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)

    @Test
    fun `uiState reflects categories emitted by the repository`() {
        coEvery { categoryRepository.getAll() } returns flowOf(listOf(sampleCategory))

        val viewModel = DashboardViewModel(categoryRepository)

        assertEquals(listOf(sampleCategory), viewModel.uiState.value.categories)
    }

    @Test
    fun `requestDelete fetches the impact and populates pendingDelete`() {
        coEvery { categoryRepository.getAll() } returns flowOf(emptyList())
        val impact = CategoryDeleteImpact(lessonCount = 4, sessionCount = 12, recordCount = 96)
        coEvery { categoryRepository.getDeleteImpact(1L) } returns impact
        val viewModel = DashboardViewModel(categoryRepository)

        viewModel.requestDelete(sampleCategory)

        assertEquals(sampleCategory, viewModel.uiState.value.pendingDelete?.category)
        assertEquals(impact, viewModel.uiState.value.pendingDelete?.impact)
    }

    @Test
    fun `confirmDelete deletes the pending category and clears pendingDelete`() {
        coEvery { categoryRepository.getAll() } returns flowOf(emptyList())
        val impact = CategoryDeleteImpact(lessonCount = 0, sessionCount = 0, recordCount = 0)
        coEvery { categoryRepository.getDeleteImpact(1L) } returns impact
        coEvery { categoryRepository.delete(sampleCategory) } returns Unit
        val viewModel = DashboardViewModel(categoryRepository)
        viewModel.requestDelete(sampleCategory)

        viewModel.confirmDelete()

        coVerify { categoryRepository.delete(sampleCategory) }
        assertNull(viewModel.uiState.value.pendingDelete)
    }

    @Test
    fun `cancelDelete clears pendingDelete without deleting`() {
        coEvery { categoryRepository.getAll() } returns flowOf(emptyList())
        val impact = CategoryDeleteImpact(lessonCount = 0, sessionCount = 0, recordCount = 0)
        coEvery { categoryRepository.getDeleteImpact(1L) } returns impact
        val viewModel = DashboardViewModel(categoryRepository)
        viewModel.requestDelete(sampleCategory)

        viewModel.cancelDelete()

        assertNull(viewModel.uiState.value.pendingDelete)
    }
}
