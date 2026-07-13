package com.example.lessonmonitor.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `verify succeeds for the correct password`() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("correct-horse-battery-staple", salt)

        assertTrue(PasswordHasher.verify("correct-horse-battery-staple", salt, hash))
    }

    @Test
    fun `verify fails for an incorrect password`() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash("correct-horse-battery-staple", salt)

        assertFalse(PasswordHasher.verify("wrong-password", salt, hash))
    }

    @Test
    fun `same password with different salts produces different hashes`() {
        val saltA = PasswordHasher.generateSalt()
        val saltB = PasswordHasher.generateSalt()

        assertNotEquals(saltA, saltB)
        assertNotEquals(PasswordHasher.hash("same-password", saltA), PasswordHasher.hash("same-password", saltB))
    }

    @Test
    fun `hashing is deterministic for the same password and salt`() {
        val salt = PasswordHasher.generateSalt()

        assertEquals(PasswordHasher.hash("pin1234", salt), PasswordHasher.hash("pin1234", salt))
    }
}
