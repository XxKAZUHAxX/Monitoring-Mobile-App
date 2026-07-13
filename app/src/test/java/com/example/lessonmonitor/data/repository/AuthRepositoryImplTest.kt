package com.example.lessonmonitor.data.repository

import com.example.lessonmonitor.data.datastore.CredentialStore
import com.example.lessonmonitor.data.datastore.SessionPreferences
import com.example.lessonmonitor.data.local.dao.UserDao
import com.example.lessonmonitor.data.local.entity.UserEntity
import com.example.lessonmonitor.util.PasswordHasher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private val credentialStore: CredentialStore = mockk()
    private val sessionPreferences: SessionPreferences = mockk()
    private val userDao: UserDao = mockk()

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        repository = AuthRepositoryImpl(credentialStore, sessionPreferences, userDao)
    }

    @Test
    fun `createCredential hashes the password, persists it, creates the user row, and logs in`() = runTest {
        val hashSlot = slot<String>()
        val saltSlot = slot<String>()
        every { credentialStore.saveCredential(capture(hashSlot), capture(saltSlot)) } returns Unit
        coEvery { userDao.upsert(any()) } returns Unit
        coEvery { sessionPreferences.setLoggedIn(true) } returns Unit

        repository.createCredential("my-password")

        assertTrue(PasswordHasher.verify("my-password", saltSlot.captured, hashSlot.captured))
        coVerify { userDao.upsert(match { it.id == UserEntity.SINGLETON_ID }) }
        coVerify { sessionPreferences.setLoggedIn(true) }
    }

    @Test
    fun `verifyPassword returns true for the correct password`() = runTest {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("secret", salt)
        every { credentialStore.getHash() } returns hash
        every { credentialStore.getSalt() } returns salt

        assertTrue(repository.verifyPassword("secret"))
    }

    @Test
    fun `verifyPassword returns false when no credential has been created yet`() = runTest {
        every { credentialStore.getHash() } returns null
        every { credentialStore.getSalt() } returns null

        assertFalse(repository.verifyPassword("anything"))
    }

    @Test
    fun `logout clears only the session flag`() = runTest {
        coEvery { sessionPreferences.setLoggedIn(false) } returns Unit

        repository.logout()

        coVerify { sessionPreferences.setLoggedIn(false) }
    }

    @Test
    fun `setBiometricEnabled creates the user row if none exists yet`() = runTest {
        coEvery { userDao.getOnce() } returns null
        coEvery { userDao.upsert(any()) } returns Unit

        repository.setBiometricEnabled(true)

        coVerify { userDao.upsert(match { it.biometricEnabled }) }
    }

    @Test
    fun `setBiometricEnabled preserves the existing row's other fields`() = runTest {
        val existing = UserEntity(biometricEnabled = false, createdAt = 123L)
        coEvery { userDao.getOnce() } returns existing
        coEvery { userDao.upsert(any()) } returns Unit

        repository.setBiometricEnabled(true)

        coVerify { userDao.upsert(match { it.biometricEnabled && it.createdAt == 123L }) }
    }
}
