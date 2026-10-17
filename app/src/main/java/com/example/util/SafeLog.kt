package com.example.util

import android.util.Log

/**
 * Sanitized logger that strips sensitive credentials, tokens, and keys.
 * Ensures passwords, bearer tokens, API keys, and JWTs are never written
 * to logcat or persisted diagnostics.
 */
object SafeLog {

    private val SENSITIVE_PATTERNS = listOf(
        Regex("(?i)password[\"':=\\s]+([^\"&\\s,]+)") to "password=[REDACTED]",
        Regex("(?i)bearer\\s+([A-Za-z0-9._-]+)") to "Bearer [REDACTED]",
        Regex("(?i)apikey[\"':=\\s]+([^\"&\\s,]+)") to "apikey=[REDACTED]",
        Regex("sb_publishable_[A-Za-z0-9_-]+") to "[REDACTED_PUBLISHABLE_KEY]",
        Regex("sb_secret_[A-Za-z0-9_-]+") to "[REDACTED_SECRET_KEY]",
        Regex("eyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]+") to "[JWT_REDACTED]"
    )

    fun sanitize(message: String?): String {
        if (message == null) return ""
        var sanitized: String = message
        for ((pattern, replacement) in SENSITIVE_PATTERNS) {
            sanitized = pattern.replace(sanitized, replacement)
        }
        return sanitized
    }

    fun d(tag: String, message: String) {
        Log.d(tag, sanitize(message))
    }

    fun i(tag: String, message: String) {
        Log.i(tag, sanitize(message))
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val safeMsg = sanitize(message)
        val safeThrowable = throwable?.let { "${it.javaClass.simpleName}: ${sanitize(it.message)}" }
        Log.w(tag, if (safeThrowable != null) "$safeMsg | $safeThrowable" else safeMsg)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val safeMsg = sanitize(message)
        val safeThrowable = throwable?.let { "${it.javaClass.simpleName}: ${sanitize(it.message)}" }
        Log.e(tag, if (safeThrowable != null) "$safeMsg | $safeThrowable" else safeMsg)
    }
}
