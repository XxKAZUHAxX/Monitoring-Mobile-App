package com.example.lessonmonitor.ui.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.lessonmonitor.ui.components.PlaceholderScreen

@Composable
fun SettingsScreen(
    onExportClick: () -> Unit,
    onBackupRestoreClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    PlaceholderScreen(
        title = "Settings",
        description = "Dark mode toggle, biometric toggle, and real logout (clearing the DataStore " +
            "session flag) land in the Dark Mode and User Account milestones."
    ) {
        Button(onClick = onExportClick, modifier = Modifier.fillMaxWidth()) {
            Text("Export")
        }
        Button(onClick = onBackupRestoreClick, modifier = Modifier.fillMaxWidth()) {
            Text("Backup / Restore")
        }
        Button(onClick = onLoggedOut, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }
    }
}
