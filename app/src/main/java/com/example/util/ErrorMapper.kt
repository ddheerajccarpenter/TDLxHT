package com.example.util

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized technical error mapper.
 * Transforms raw Supabase/HTTP/PostgreSQL/Kotlin exceptions into controlled,
 * safe, human-readable application errors. Strips all API keys, URLs, headers,
 * and technical diagnostic details from reaching the UI.
 */
object ErrorMapper {

    fun mapSignIn(throwable: Throwable?, inputEmail: String? = null): SafeAppError {
        if (throwable == null) return SafeAppError.UnableToSignIn
        val raw = extractRawText(throwable)

        return when {
            raw.contains("email_not_confirmed", ignoreCase = true) ||
            raw.contains("confirm your email", ignoreCase = true) ||
            raw.contains("Email not confirmed", ignoreCase = true) -> {
                SafeAppError.EmailNotConfirmed(inputEmail)
            }
            raw.contains("invalid_credentials", ignoreCase = true) ||
            raw.contains("invalid login credentials", ignoreCase = true) ||
            raw.contains("invalid_grant", ignoreCase = true) ||
            raw.contains("Wrong password", ignoreCase = true) -> {
                SafeAppError.InvalidCredentials
            }
            raw.contains("session expired", ignoreCase = true) ||
            raw.contains("refresh_token_not_found", ignoreCase = true) ||
            raw.contains("invalid_refresh_token", ignoreCase = true) ||
            raw.contains("jwt expired", ignoreCase = true) -> {
                SafeAppError.SessionExpired
            }
            raw.contains("rate_limit", ignoreCase = true) ||
            raw.contains("too many requests", ignoreCase = true) ||
            raw.contains("over_email_send_rate_limit", ignoreCase = true) ||
            raw.contains("429") -> {
                SafeAppError.RateLimited
            }
            isNetworkError(throwable, raw) -> {
                mapNetwork(throwable)
            }
            else -> {
                SafeAppError.UnableToSignIn
            }
        }
    }

    fun mapSignUp(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.UnableToSignUp
        val raw = extractRawText(throwable)

        return when {
            raw.contains("user_already_exists", ignoreCase = true) ||
            raw.contains("already registered", ignoreCase = true) ||
            raw.contains("User already registered", ignoreCase = true) -> {
                SafeAppError.AccountAlreadyExists
            }
            raw.contains("weak_password", ignoreCase = true) ||
            raw.contains("Password should be at least", ignoreCase = true) ||
            raw.contains("password is too weak", ignoreCase = true) -> {
                SafeAppError.WeakPassword
            }
            raw.contains("invalid email", ignoreCase = true) ||
            raw.contains("valid email", ignoreCase = true) ||
            raw.contains("Unable to validate email address", ignoreCase = true) -> {
                SafeAppError.InvalidEmail
            }
            raw.contains("rate_limit", ignoreCase = true) ||
            raw.contains("over_email_send_rate_limit", ignoreCase = true) ||
            raw.contains("429") -> {
                SafeAppError.RateLimited
            }
            isNetworkError(throwable, raw) -> {
                mapNetwork(throwable)
            }
            else -> {
                SafeAppError.UnableToSignUp
            }
        }
    }

    fun mapPasswordReset(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.PasswordResetFailed
        val raw = extractRawText(throwable)

        return when {
            raw.contains("invalid email", ignoreCase = true) ||
            raw.contains("valid email", ignoreCase = true) -> {
                SafeAppError.InvalidEmail
            }
            raw.contains("rate_limit", ignoreCase = true) ||
            raw.contains("over_email_send_rate_limit", ignoreCase = true) -> {
                SafeAppError.RateLimited
            }
            isNetworkError(throwable, raw) -> {
                SafeAppError.UnableToSendResetEmail
            }
            else -> {
                SafeAppError.PasswordResetFailed
            }
        }
    }

    fun mapResendConfirmation(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.ResendConfirmationFailed
        val raw = extractRawText(throwable)
        return when {
            raw.contains("rate_limit", ignoreCase = true) ||
            raw.contains("over_email_send_rate_limit", ignoreCase = true) ||
            raw.contains("429") -> {
                SafeAppError.RateLimited
            }
            isNetworkError(throwable, raw) -> {
                SafeAppError.NoInternet
            }
            else -> {
                SafeAppError.ResendConfirmationFailed
            }
        }
    }

    fun mapDatabase(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.SaveFailed
        val raw = extractRawText(throwable)

        return when {
            raw.contains("permission", ignoreCase = true) ||
            raw.contains("denied", ignoreCase = true) ||
            raw.contains("row-level security", ignoreCase = true) ||
            raw.contains("violates row-level security", ignoreCase = true) ||
            raw.contains("RLS", ignoreCase = true) ||
            raw.contains("403") -> {
                SafeAppError.AccessDenied
            }
            isNetworkError(throwable, raw) -> {
                mapNetwork(throwable)
            }
            else -> {
                SafeAppError.SaveFailed
            }
        }
    }

    fun mapSync(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.SyncFailed
        val raw = extractRawText(throwable)

        return when {
            isNetworkError(throwable, raw) -> SafeAppError.SyncPaused
            raw.contains("auth", ignoreCase = true) || raw.contains("unauthorized", ignoreCase = true) -> SafeAppError.SessionExpired
            else -> SafeAppError.SyncFailed
        }
    }

    fun mapNetwork(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.UnableToConnect
        val raw = extractRawText(throwable)

        return when {
            throwable is UnknownHostException ||
            raw.contains("UnknownHostException", ignoreCase = true) ||
            raw.contains("No address associated", ignoreCase = true) ||
            raw.contains("Unable to resolve host", ignoreCase = true) -> {
                SafeAppError.NoInternet
            }
            throwable is SocketTimeoutException ||
            raw.contains("timeout", ignoreCase = true) ||
            raw.contains("timed out", ignoreCase = true) -> {
                SafeAppError.ConnectionTimeout
            }
            throwable is ConnectException ||
            raw.contains("503") ||
            raw.contains("502") ||
            raw.contains("Service Unavailable", ignoreCase = true) -> {
                SafeAppError.ServiceUnavailable
            }
            else -> {
                SafeAppError.UnableToConnect
            }
        }
    }

    fun mapEdgeFunction(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.General("Something went wrong")
        val raw = extractRawText(throwable)

        return when {
            raw.contains("401", ignoreCase = true) || raw.contains("unauthorized", ignoreCase = true) -> {
                SafeAppError.EdgeFunctionAuthRequired
            }
            raw.contains("403", ignoreCase = true) || raw.contains("forbidden", ignoreCase = true) -> {
                SafeAppError.AccessDenied
            }
            raw.contains("400", ignoreCase = true) -> {
                SafeAppError.EdgeFunctionInvalidRequest
            }
            raw.contains("503", ignoreCase = true) || raw.contains("502", ignoreCase = true) -> {
                SafeAppError.ServiceUnavailable
            }
            raw.contains("timeout", ignoreCase = true) -> {
                SafeAppError.ConnectionTimeout
            }
            isNetworkError(throwable, raw) -> {
                mapNetwork(throwable)
            }
            else -> {
                SafeAppError.General("Something went wrong")
            }
        }
    }

    fun mapNotification(throwable: Throwable?): SafeAppError {
        if (throwable == null) return SafeAppError.NotificationScheduleFailed
        val raw = extractRawText(throwable)

        return when {
            raw.contains("permission", ignoreCase = true) ||
            raw.contains("denied", ignoreCase = true) ||
            raw.contains("SecurityException", ignoreCase = true) -> {
                SafeAppError.NotificationsDisabled
            }
            else -> {
                SafeAppError.NotificationScheduleFailed
            }
        }
    }

    fun mapNapkin(throwable: Throwable?): SafeAppError {
        return SafeAppError.NapkinGenerationFailed
    }

    fun mapGeneral(throwable: Throwable?, fallbackTitle: String = "Something went wrong"): SafeAppError {
        if (throwable == null) return SafeAppError.General(fallbackTitle)
        val raw = extractRawText(throwable)
        return when {
            isNetworkError(throwable, raw) -> mapNetwork(throwable)
            else -> SafeAppError.General(fallbackTitle)
        }
    }

    private fun isNetworkError(throwable: Throwable?, raw: String): Boolean {
        if (throwable is UnknownHostException ||
            throwable is SocketTimeoutException ||
            throwable is ConnectException ||
            throwable is java.net.SocketException) {
            return true
        }
        return raw.contains("network", ignoreCase = true) ||
               raw.contains("connection", ignoreCase = true) ||
               raw.contains("timeout", ignoreCase = true) ||
               raw.contains("timed out", ignoreCase = true) ||
               raw.contains("unknownhost", ignoreCase = true) ||
               raw.contains("connectexception", ignoreCase = true) ||
               raw.contains("no route to host", ignoreCase = true)
    }

    private fun extractRawText(throwable: Throwable): String {
        return buildString {
            append(throwable::class.java.simpleName)
            append(" ")
            append(throwable.message.orEmpty())
            var cause = throwable.cause
            var depth = 0
            while (cause != null && depth < 3) {
                append(" ")
                append(cause::class.java.simpleName)
                append(" ")
                append(cause.message.orEmpty())
                cause = cause.cause
                depth++
            }
        }
    }
}
