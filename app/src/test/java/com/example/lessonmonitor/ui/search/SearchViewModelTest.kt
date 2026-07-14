package com.example.lessonmonitor.ui.search

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.local.entity.CategoryEntity
import com.example.lessonmonitor.data.local.entity.LessonEntity
import com.example.lessonmonitor.data.local.entity.StudentEntity
import com.example.lessonmonitor.domain.repository.CategoryRepository
import com.example.lessonmonitor.domain.repository.LessonRepository
import com.example.lessonmonitor.domain.repository.StudentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val categoryRepository: CategoryRepository = mockk()
    private val lessonRepository: LessonRepository = mockk()
    private val studentRepository: StudentRepository = mockk()

    private fun buildViewModel() = SearchViewModel(categoryRepository, lessonRepository, studentRepository)

    @Test
    fun `blank query clears all results without querying the repositories`() {
        val viewModel = buildViewModel()

        viewModel.onQueryChange("")

        assertEquals(emptyList<CategoryEntity>(), viewModel.uiState.value.categoryResults)
        assertEquals(emptyList<LessonEntity>(), viewModel.uiState.value.lessonResults)
        assertEquals(emptyList<StudentEntity>(), viewModel.uiState.value.studentResults)
        verify(exactly = 0) { categoryRepository.search(any()) }
        verify(exactly = 0) { lessonRepository.search(any()) }
        verify(exactly = 0) { studentRepository.search(any()) }
    }

    @Test
    fun `non-blank query combines results from all three repositories`() {
        val category = CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)
        val lesson = LessonEntity(id = 1L, categoryId = 1L, title = "Math basics", startDate = 1L, createdAt = 1L, updatedAt = 1L)
        val student = StudentEntity(id = 1L, name = "Matheus", createdAt = 1L, updatedAt = 1L)
        every { categoryRepository.search("mat") } returns flowOf(listOf(category))
        every { lessonRepository.search("mat") } returns flowOf(listOf(lesson))
        every { studentRepository.search("mat") } returns flowOf(listOf(student))
        val viewModel = buildViewModel()

        viewModel.onQueryChange("mat")

        assertEquals(listOf(category), viewModel.uiState.value.categoryResults)
        assertEquals(listOf(lesson), viewModel.uiState.value.lessonResults)
        assertEquals(listOf(student), viewModel.uiState.value.studentResults)
    }

    @Test
    fun `clearing the query after a search resets all result lists`() {
        every { categoryRepository.search("mat") } returns flowOf(listOf(CategoryEntity(id = 1L, name = "Math", createdAt = 1L, updatedAt = 1L)))
        every { lessonRepository.search("mat") } returns flowOf(emptyList())
        every { studentRepository.search("mat") } returns flowOf(emptyList())
        val viewModel = buildViewModel()
        viewModel.onQueryChange("mat")

        viewModel.onQueryChange("")

        assertEquals(emptyList<CategoryEntity>(), viewModel.uiState.value.categoryResults)
        assertEquals("", viewModel.uiState.value.query)
    }
}
