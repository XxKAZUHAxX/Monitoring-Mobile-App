package com.example.lessonmonitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared shell for every screen whose real feature milestone hasn't landed
 * yet. Renders a title, a short description of what will eventually live
 * here (and which milestone builds it, per PLAN.md §7), and any navigation
 * actions needed to keep the graph shape testable end to end. Screens are
 * migrated off this one-by-one as each feature milestone is implemented.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }) }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
            actions()
        }
    }
}
