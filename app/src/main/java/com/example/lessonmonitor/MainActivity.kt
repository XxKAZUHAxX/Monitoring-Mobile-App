package com.example.lessonmonitor

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.lessonmonitor.data.datastore.ThemeMode
import com.example.lessonmonitor.data.datastore.ThemePreferences
import com.example.lessonmonitor.navigation.LessonMonitorNavHost
import com.example.lessonmonitor.ui.theme.LessonMonitorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Extends `FragmentActivity` (not plain `ComponentActivity`) because
 * androidx.biometric's `BiometricPrompt` requires one — see
 * `ui/auth/BiometricAuth.kt` and PLAN.md §6. `FragmentActivity` is itself a
 * `ComponentActivity`, so `setContent`/`enableEdgeToEdge` still apply.
 *
 * Theme mode is read from [ThemePreferences] (DataStore) and passed to
 * [LessonMonitorTheme] so the user's manual override (set in Settings)
 * controls the light/dark appearance.
 *
 * Deep-link intents (from notification taps) carry a
 * `lessonmonitor://lesson/{lessonId}/session/{sessionId}` URI and are
 * forwarded to the outer [NavHostController] via [NavHostController.handleDeepLink]
 * both at initial launch and via [onNewIntent] when the app is already running.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var themePreferences: ThemePreferences

    /** Set by [LessonMonitorNavHost] so [onNewIntent] can forward deep links. */
    private val navControllerRef = mutableStateOf<NavHostController?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

            // Forward the launch intent once the NavController is ready.
            val launchIntent = intent
            LaunchedEffect(navControllerRef.value, launchIntent) {
                val nc = navControllerRef.value ?: return@LaunchedEffect
                if (launchIntent?.data != null) {
                    nc.handleDeepLink(launchIntent)
                }
            }
            LessonMonitorTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LessonMonitorNavHost(
                        onNavControllerReady = { navControllerRef.value = it }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navControllerRef.value?.handleDeepLink(intent)
    }
}
