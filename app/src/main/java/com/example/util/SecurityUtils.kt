package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.trim().toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, hash: String): Boolean {
        return hashPin(pin) == hash
    }
}
