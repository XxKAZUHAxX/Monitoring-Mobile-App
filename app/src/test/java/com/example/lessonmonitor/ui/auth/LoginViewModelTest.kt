package com.example.lessonmonitor.ui.auth

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()

    private fun createViewModel(biometricEnabled: Boolean = false): LoginViewModel {
        every { authRepository.isBiometricEnabled() } returns flowOf(biometricEnabled)
        return LoginViewModel(authRepository)
    }

    @Test
    fun `submit logs in and calls onSuccess when the password is correct`() {
        coEvery { authRepository.verifyPassword("correct") } returns true
        coEvery { authRepository.setLoggedIn(true) } returns Unit
        val viewModel = createViewModel()
        viewModel.onPasswordChange("correct")

        var loggedIn = false
        viewModel.submit { loggedIn = true }

        assertTrue(loggedIn)
        coVerify { authRepository.setLoggedIn(true) }
    }

    @Test
    fun `submit shows an error and does not call onSuccess when the password is wrong`() {
        coEvery { authRepository.verifyPassword("wrong") } returns false
        val viewModel = createViewModel()
        viewModel.onPasswordChange("wrong")

        var loggedIn = false
        viewModel.submit { loggedIn = true }

        assertEquals(false, loggedIn)
        assertEquals("Incorrect password", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `uiState reflects the biometricEnabled flag from the repository`() {
        val viewModel = createViewModel(biometricEnabled = true)

        assertTrue(viewModel.uiState.value.biometricEnabled)
    }
}
