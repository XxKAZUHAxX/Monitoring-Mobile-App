package com.example.lessonmonitor.ui.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Thin wrapper around androidx.biometric so screens don't need to repeat the
 * BiometricPrompt callback boilerplate. Requires a [FragmentActivity], which
 * is why `MainActivity` extends `FragmentActivity` instead of a plain
 * `ComponentActivity` (PLAN.md §1 assumption #2 / Feature A).
 */
fun isBiometricAvailable(activity: FragmentActivity): Boolean =
    BiometricManager.from(activity)
        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS

fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (!isBiometricAvailable(activity)) {
        onError("Biometric unlock isn't available on this device right now.")
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Lesson Monitor")
        .setSubtitle("Use your fingerprint or face to continue")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .setNegativeButtonText("Use password instead")
        .build()

    prompt.authenticate(promptInfo)
}
