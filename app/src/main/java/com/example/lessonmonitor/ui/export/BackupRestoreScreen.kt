package com.example.lessonmonitor.ui.export

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.util.writeAndShareFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(viewModel: BackupRestoreViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        val content = uri?.let { context.contentResolver.openInputStream(it)?.use { stream -> stream.readBytes().decodeToString() } }
        if (content != null) viewModel.onFileContentPicked(content)
    }

    LaunchedEffect(uiState.pendingExport) {
        uiState.pendingExport?.let { pending ->
            writeAndShareFile(context, pending.fileName, pending.content, "application/json")
            viewModel.onExportHandled()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Backup & Restore") }) }) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Export a JSON snapshot of the whole database, or restore from a previously exported file.")
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            uiState.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            Button(
                onClick = viewModel::exportBackup,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export backup")
            }
            OutlinedButton(
                onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore from file")
            }
            if (uiState.isBusy) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    uiState.pendingRestore?.let { snapshot ->
        AlertDialog(
            onDismissRequest = viewModel::cancelRestore,
            title = { Text("Restore backup?") },
            text = {
                Text(
                    "This will permanently replace all current data with this file's contents: " +
                        "${snapshot.categories.size} categories, ${snapshot.lessons.size} lessons, " +
                        "${snapshot.students.size} students, " +
                        "${snapshot.attendanceRecords.size} attendance records. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmRestore) {
                    Text("Restore", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelRestore) { Text("Cancel") }
            }
        )
    }
}

