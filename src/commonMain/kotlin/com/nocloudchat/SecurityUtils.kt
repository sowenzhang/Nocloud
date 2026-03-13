package com.nocloudchat

/** Returns the lowercase hexadecimal SHA-256 digest of [input] (UTF-8 encoded). */
internal fun sha256(input: String): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
    return hash.joinToString("") { "%02x".format(it) }
}
