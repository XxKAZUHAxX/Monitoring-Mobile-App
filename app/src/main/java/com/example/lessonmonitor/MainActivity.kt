package com.example.lessonmonitor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.lessonmonitor.navigation.LessonMonitorNavHost
import com.example.lessonmonitor.ui.theme.LessonMonitorTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Extends `FragmentActivity` (not plain `ComponentActivity`) because
 * androidx.biometric's `BiometricPrompt` requires one — see
 * `ui/auth/BiometricAuth.kt` and PLAN.md §6. `FragmentActivity` is itself a
 * `ComponentActivity`, so `setContent`/`enableEdgeToEdge` still apply.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LessonMonitorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LessonMonitorNavHost()
                }
            }
        }
    }
}

