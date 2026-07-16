package com.example.lessonmonitor.ui.export

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.domain.repository.BackupRepository
import com.example.lessonmonitor.domain.repository.BackupSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class BackupRestoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val backupRepository: BackupRepository = mockk()

    private fun buildViewModel() = BackupRestoreViewModel(backupRepository)

    private fun emptySnapshot(exportedAt: Long = 1L) = BackupSnapshot(
        schemaVersion = 1,
        exportedAt = exportedAt,
        users = emptyList(),
        categories = emptyList(),
        lessons = emptyList(),
        students = emptyList(),
        enrollments = emptyList(),
        attendanceRecords = emptyList()
    )

    @Test
    fun `exportBackup produces a pending export with a filename derived from exportedAt`() = runTest {
        val viewModel = buildViewModel()
        coEvery { backupRepository.exportSnapshot() } returns emptySnapshot(exportedAt = 42L)

        viewModel.exportBackup()

        val pending = viewModel.uiState.value.pendingExport
        assertNotNull(pending)
        assertEquals("lessonmonitor_backup_42.json", pending!!.fileName)
        assertEquals(false, viewModel.uiState.value.isBusy)
    }

    @Test
    fun `onFileContentPicked with valid JSON stages a pending restore`() {
        val viewModel = buildViewModel()
        val json = com.example.lessonmonitor.data.export.BackupJsonSerializer.encode(emptySnapshot())

        viewModel.onFileContentPicked(json)

        assertNotNull(viewModel.uiState.value.pendingRestore)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onFileContentPicked with invalid JSON sets an error instead of a pending restore`() {
        val viewModel = buildViewModel()

        viewModel.onFileContentPicked("not valid json")

        assertNull(viewModel.uiState.value.pendingRestore)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `cancelRestore clears the pending restore`() {
        val viewModel = buildViewModel()
        viewModel.onFileContentPicked(com.example.lessonmonitor.data.export.BackupJsonSerializer.encode(emptySnapshot()))

        viewModel.cancelRestore()

        assertNull(viewModel.uiState.value.pendingRestore)
    }

    @Test
    fun `confirmRestore calls restoreSnapshot and clears the pending restore`() = runTest {
        val viewModel = buildViewModel()
        val snapshot = emptySnapshot()
        viewModel.onFileContentPicked(com.example.lessonmonitor.data.export.BackupJsonSerializer.encode(snapshot))
        coEvery { backupRepository.restoreSnapshot(any()) } returns Unit

        viewModel.confirmRestore()

        coVerify { backupRepository.restoreSnapshot(snapshot) }
        assertNull(viewModel.uiState.value.pendingRestore)
        assertEquals("Restore complete.", viewModel.uiState.value.successMessage)
    }
}
