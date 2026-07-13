package com.example.lessonmonitor.util

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2WithHmacSHA256 password hashing (PLAN.md §6 Security decision). The
 * local credential is never stored — or compared — as plaintext or a
 * reversible value.
 *
 * Uses `java.util.Base64` rather than `android.util.Base64` — minSdk is 26,
 * where `java.util.Base64` is guaranteed available, and unlike the Android
 * framework class it works in plain JVM unit tests without Robolectric.
 */
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16

    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hash(password: String, salt: String): String {
        val saltBytes = Base64.getDecoder().decode(salt)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    /** Constant-time comparison to avoid leaking timing information about the stored hash. */
    fun verify(password: String, salt: String, expectedHash: String): Boolean {
        val actualHash = hash(password, salt)
        if (actualHash.length != expectedHash.length) return false
        var diff = 0
        for (i in actualHash.indices) {
            diff = diff or (actualHash[i].code xor expectedHash[i].code)
        }
        return diff == 0
    }
}

