package com.example.lessonmonitor.ui.student

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.navigation.Routes
import com.example.lessonmonitor.util.copyUriToInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun StudentFormScreen(
    studentId: Long,
    onDone: () -> Unit,
    viewModel: StudentFormViewModel = hiltViewModel()
) {
    LaunchedEffect(studentId) { viewModel.load(studentId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isNew = studentId == Routes.NEW_ID
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val path = withContext(Dispatchers.IO) { copyUriToInternalStorage(context, uri) }
                if (path != null) viewModel.onPhotoPathChange(path)
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(if (isNew) "Add Student" else "Edit Student") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val photoBitmap = remember(uiState.photoPath) {
                uiState.photoPath?.let { path -> BitmapFactory.decodeFile(path)?.asImageBitmap() }
            }
            if (photoBitmap != null) {
                Image(
                    bitmap = photoBitmap,
                    contentDescription = "Student photo",
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
            }
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text(if (uiState.photoPath == null) "Choose photo" else "Change photo")
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = { viewModel.submit(onDone) },
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSubmitting) "Saving…" else "Save")
            }
        }
    }
}

