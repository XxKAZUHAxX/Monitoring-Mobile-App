package com.example.lessonmonitor.ui.settings

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.data.datastore.ThemeMode
import com.example.lessonmonitor.data.datastore.ThemePreferences
import com.example.lessonmonitor.domain.repository.AuthRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private val themePreferences: ThemePreferences = mockk()

    private val biometricFlow = MutableStateFlow(false)
    private val themeModeFlow = MutableStateFlow(ThemeMode.SYSTEM)

    @Before
    fun setUp() {
        every { authRepository.isBiometricEnabled() } returns biometricFlow
        every { themePreferences.themeMode } returns themeModeFlow
        coEvery { authRepository.setBiometricEnabled(any()) } returns Unit
        coEvery { authRepository.logout() } returns Unit
        coEvery { themePreferences.setThemeMode(any()) } returns Unit
    }

    private fun buildViewModel() = SettingsViewModel(authRepository, themePreferences)

    // ---- Theme mode ----

    @Test
    fun `themeMode starts with the value from preferences`() {
        val vm = buildViewModel()
        assertEquals(ThemeMode.SYSTEM, vm.themeMode.value)
    }

    @Test
    fun `themeMode reflects preference changes`() {
        val vm = buildViewModel()
        themeModeFlow.value = ThemeMode.DARK
        assertEquals(ThemeMode.DARK, vm.themeMode.value)
    }

    @Test
    fun `setThemeMode writes to preferences`() = runTest {
        val vm = buildViewModel()
        vm.setThemeMode(ThemeMode.LIGHT)
        coVerify(exactly = 1) { themePreferences.setThemeMode(ThemeMode.LIGHT) }
    }

    // ---- Biometric ----

    @Test
    fun `biometricEnabled starts as false`() {
        val vm = buildViewModel()
        assertEquals(false, vm.biometricEnabled.value)
    }

    @Test
    fun `setBiometricEnabled writes to auth repository`() = runTest {
        val vm = buildViewModel()
        vm.setBiometricEnabled(true)
        coVerify(exactly = 1) { authRepository.setBiometricEnabled(true) }
    }

    // ---- Logout ----

    @Test
    fun `logout calls authRepository logout then invokes callback`() = runTest {
        val vm = buildViewModel()
        var called = false
        vm.logout { called = true }
        coVerify(exactly = 1) { authRepository.logout() }
        assertEquals(true, called)
    }
}
