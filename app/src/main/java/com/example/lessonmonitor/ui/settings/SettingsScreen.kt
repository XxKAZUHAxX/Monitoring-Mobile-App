package com.example.lessonmonitor.ui.settings

import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun SettingsScreen(
    onExportClick: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val biometricAvailable = remember {
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    PlaceholderScreen(
        title = "Settings",
        description = "Dark mode toggle lands in the Dark Mode milestone."
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Biometric unlock", modifier = Modifier.weight(1f))
            Switch(
                checked = biometricEnabled,
                onCheckedChange = viewModel::setBiometricEnabled,
                enabled = biometricAvailable
            )
        }
        if (!biometricAvailable) {
            Text(
                text = "No biometric hardware/enrollment detected on this device.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(onClick = onExportClick, modifier = Modifier.fillMaxWidth()) {
            Text("Export")
        }
        Button(onClick = onBackupRestoreClick, modifier = Modifier.fillMaxWidth()) {
            Text("Backup / Restore")
        }
        Button(onClick = { viewModel.logout(onLoggedOut) }, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }
    }
}

