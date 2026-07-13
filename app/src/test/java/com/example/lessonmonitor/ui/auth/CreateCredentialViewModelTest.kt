package com.example.lessonmonitor.ui.auth

import com.example.lessonmonitor.MainDispatcherRule
import com.example.lessonmonitor.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CreateCredentialViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()

    @Test
    fun `submit fails validation when the password is too short`() {
        val viewModel = CreateCredentialViewModel(authRepository)
        viewModel.onPasswordChange("abc")
        viewModel.onConfirmPasswordChange("abc")

        var onSuccessCalled = false
        viewModel.submit { onSuccessCalled = true }

        assertEquals(false, onSuccessCalled)
        assertEquals("Password must be at least 4 characters", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit fails validation when passwords don't match`() {
        val viewModel = CreateCredentialViewModel(authRepository)
        viewModel.onPasswordChange("password1")
        viewModel.onConfirmPasswordChange("password2")

        var onSuccessCalled = false
        viewModel.submit { onSuccessCalled = true }

        assertEquals(false, onSuccessCalled)
        assertEquals("Passwords do not match", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submit creates the credential and calls onSuccess when valid`() {
        coEvery { authRepository.createCredential("password1") } returns Unit
        val viewModel = CreateCredentialViewModel(authRepository)
        viewModel.onPasswordChange("password1")
        viewModel.onConfirmPasswordChange("password1")

        var onSuccessCalled = false
        viewModel.submit { onSuccessCalled = true }

        assertEquals(true, onSuccessCalled)
        assertNull(viewModel.uiState.value.errorMessage)
        coVerify { authRepository.createCredential("password1") }
    }
}
