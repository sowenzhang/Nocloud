package com.nocloudchat

import kotlin.test.Test
import kotlin.test.assertEquals

class SecurityUtilsTest {

    // Known SHA-256 vectors (verified against standard implementations)
    @Test
    fun `sha256 of empty string matches known vector`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sha256(""),
        )
    }

    @Test
    fun `sha256 of 'hello' matches known vector`() {
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            sha256("hello"),
        )
    }

    @Test
    fun `sha256 of 'NoCloudChat' matches known vector`() {
        assertEquals(
            "4ae001b85e56d1c6b5000c95cab49fd2810cb3276f936683d0f68c95d680edd2",
            sha256("NoCloudChat"),
        )
    }

    @Test
    fun `sha256 output is always 64 lowercase hex characters`() {
        listOf("", "a", "secret passphrase", "12345", "🔐").forEach { input ->
            val digest = sha256(input)
            assertEquals(64, digest.length, "Expected 64 hex chars for input '$input'")
            assert(digest.all { it in '0'..'9' || it in 'a'..'f' }) {
                "sha256 output should be lowercase hex for input '$input', got '$digest'"
            }
        }
    }

    @Test
    fun `sha256 is deterministic — same input produces same output`() {
        val passphrase = "my-secret-network-key"
        assertEquals(sha256(passphrase), sha256(passphrase))
    }

    @Test
    fun `sha256 is sensitive to case`() {
        val lower = sha256("secret")
        val upper = sha256("Secret")
        assert(lower != upper) { "SHA-256 should differ for different cases" }
    }

    @Test
    fun `sha256 handles unicode input correctly`() {
        // Ensure we get a valid 64-char hex string for unicode
        val result = sha256("パスワード")
        assertEquals(64, result.length)
    }
}
