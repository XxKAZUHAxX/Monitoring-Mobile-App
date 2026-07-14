package com.example.lessonmonitor.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lessonmonitor.data.export.BackupJsonSerializer
import com.example.lessonmonitor.domain.repository.BackupRepository
import com.example.lessonmonitor.domain.repository.BackupSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import javax.inject.Inject

/**
 * Whole-database JSON backup/restore (PLAN.md §7 milestone 13). Like
 * [ExportViewModel], the actual file write/share and file-picker read are
 * Android-framework-dependent and left to `BackupRestoreScreen` (via
 * `util/FileSharer.kt` and `ActivityResultContracts.OpenDocument()`) so this
 * class stays a plain JVM-testable orchestrator.
 */
@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    /** A JSON backup file ready to be written + shared by the UI layer. */
    data class PendingExport(val fileName: String, val content: String)

    data class UiState(
        val isBusy: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null,
        val pendingExport: PendingExport? = null,
        /** Non-null shows the "replace all data?" confirmation dialog. */
        val pendingRestore: BackupSnapshot? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, errorMessage = null, successMessage = null) }
            val snapshot = backupRepository.exportSnapshot()
            val content = BackupJsonSerializer.encode(snapshot)
            _uiState.update {
                it.copy(isBusy = false, pendingExport = PendingExport("lessonmonitor_backup_${snapshot.exportedAt}.json", content))
            }
        }
    }

    fun onExportHandled() = _uiState.update { it.copy(pendingExport = null) }

    /** Called with the raw text read from a user-picked `.json` file (SAF `OpenDocument`). */
    fun onFileContentPicked(content: String) {
        try {
            val snapshot = BackupJsonSerializer.decode(content)
            _uiState.update { it.copy(pendingRestore = snapshot, errorMessage = null) }
        } catch (e: SerializationException) {
            _uiState.update { it.copy(errorMessage = "That file isn't a valid backup snapshot.") }
        }
    }

    fun cancelRestore() = _uiState.update { it.copy(pendingRestore = null) }

    fun confirmRestore() {
        val snapshot = _uiState.value.pendingRestore ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, pendingRestore = null) }
            backupRepository.restoreSnapshot(snapshot)
            _uiState.update { it.copy(isBusy = false, successMessage = "Restore complete.") }
        }
    }

    fun consumeSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
}
