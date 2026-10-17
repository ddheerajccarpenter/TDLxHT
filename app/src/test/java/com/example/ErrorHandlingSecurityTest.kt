package com.example

import com.example.util.ErrorActionType
import com.example.util.ErrorMapper
import com.example.util.SafeAppError
import com.example.util.SafeLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorHandlingSecurityTest {

    @Test
    fun testEmailNotConfirmedMapping() {
        val rawException = RuntimeException(
            "HttpRequestException: {\"code\":400,\"error_code\":\"email_not_confirmed\",\"msg\":\"Email not confirmed\"} " +
            "url=https://mtlckvkyimoqbrlvirch.supabase.co/auth/v1/token?grant_type=password headers=[apikey: sb_publishable_XtQN9HZAmLO8LDllURZOww_5LPbAnjP]"
        )

        val mapped = ErrorMapper.mapSignIn(rawException, "test@example.com")
        assertTrue("Must map to EmailNotConfirmed", mapped is SafeAppError.EmailNotConfirmed)
        assertEquals("Account confirmation required", mapped.title)
        assertEquals("Please confirm your email address before signing in.", mapped.description)
        assertEquals(ErrorActionType.RESEND_CONFIRMATION, mapped.actionType)
        assertEquals("Resend confirmation email", mapped.actionLabel)
        assertEquals("test@example.com", (mapped as SafeAppError.EmailNotConfirmed).email)

        // Ensure zero raw details leaked
        assertFalse(mapped.title.contains("supabase"))
        assertFalse(mapped.title.contains("apikey"))
        assertFalse(mapped.title.contains("400"))
        assertFalse(mapped.description?.contains("http") ?: false)
    }

    @Test
    fun testInvalidCredentialsMapping() {
        val rawException = RuntimeException("RestException(error=invalid_grant, error_description=Invalid login credentials, statusCode=400)")
        val mapped = ErrorMapper.mapSignIn(rawException)
        assertEquals(SafeAppError.InvalidCredentials, mapped)
        assertEquals("Invalid email or password", mapped.title)
        assertEquals("Please check your credentials and try again.", mapped.description)
    }

    @Test
    fun testNetworkErrorsMapping() {
        val noInternet = UnknownHostException("Unable to resolve host mtlckvkyimoqbrlvirch.supabase.co: No address associated with hostname")
        val mappedNoInternet = ErrorMapper.mapSignIn(noInternet)
        assertEquals(SafeAppError.NoInternet, mappedNoInternet)
        assertEquals("No internet connection", mappedNoInternet.title)
        assertEquals(ErrorActionType.RETRY, mappedNoInternet.actionType)

        val timeout = SocketTimeoutException("timeout connecting to server")
        val mappedTimeout = ErrorMapper.mapSignIn(timeout)
        assertEquals(SafeAppError.ConnectionTimeout, mappedTimeout)
        assertEquals("Connection timed out", mappedTimeout.title)

        val connectFail = ConnectException("Failed to connect to 127.0.0.1:443")
        val mappedConnect = ErrorMapper.mapSignIn(connectFail)
        assertEquals(SafeAppError.ServiceUnavailable, mappedConnect)
        assertEquals("Service temporarily unavailable", mappedConnect.title)
    }

    @Test
    fun testRateLimitMapping() {
        val rateLimitException = RuntimeException("over_email_send_rate_limit: 429 Too Many Requests")
        val mapped = ErrorMapper.mapSignIn(rateLimitException)
        assertEquals(SafeAppError.RateLimited, mapped)
        assertEquals("Too many attempts", mapped.title)
    }

    @Test
    fun testAccountCreationErrorsMapping() {
        val userExists = RuntimeException("User already registered: user_already_exists")
        val mappedExists = ErrorMapper.mapSignUp(userExists)
        assertEquals(SafeAppError.AccountAlreadyExists, mappedExists)
        assertEquals("Account already exists", mappedExists.title)

        val weakPassword = RuntimeException("Password should be at least 6 characters (weak_password)")
        val mappedWeak = ErrorMapper.mapSignUp(weakPassword)
        assertEquals(SafeAppError.WeakPassword, mappedWeak)
        assertEquals("Password does not meet the requirements", mappedWeak.title)
    }

    @Test
    fun testDatabaseAndRlsErrorMapping() {
        val rlsException = RuntimeException("new row violates row-level security policy for table tasks")
        val mappedRls = ErrorMapper.mapDatabase(rlsException)
        assertEquals(SafeAppError.AccessDenied, mappedRls)
        assertEquals("Access denied", mappedRls.title)

        val generalDb = RuntimeException("sqlite error table corrupted")
        val mappedGeneral = ErrorMapper.mapDatabase(generalDb)
        assertEquals(SafeAppError.SaveFailed, mappedGeneral)
        assertEquals("Unable to save changes", mappedGeneral.title)
    }

    @Test
    fun testSyncErrorMapping() {
        val syncOffline = UnknownHostException("No address associated")
        val mappedSync = ErrorMapper.mapSync(syncOffline)
        assertEquals(SafeAppError.SyncPaused, mappedSync)
        assertEquals("Sync paused", mappedSync.title)

        val generalSync = IOException("Disk read failed")
        val mappedGeneral = ErrorMapper.mapSync(generalSync)
        assertEquals(SafeAppError.SyncFailed, mappedGeneral)
        assertEquals("Unable to sync changes", mappedGeneral.title)
    }

    @Test
    fun testNapkinErrorMapping() {
        val napkinErr = RuntimeException("HTTP 500 from https://api.napkin.ai/v1/visuals?token=secret_12345")
        val mapped = ErrorMapper.mapNapkin(napkinErr)
        assertEquals(SafeAppError.NapkinGenerationFailed, mapped)
        assertEquals("Unable to generate report visual", mapped.title)
        assertEquals("Productivity metrics are still fully available.", mapped.description)
        assertEquals(ErrorActionType.RETRY, mapped.actionType)
        assertEquals("Try Again", mapped.actionLabel)

        // Strict verification: zero leak of napkin url or token
        assertFalse(mapped.title.contains("napkin.ai"))
        assertFalse(mapped.description?.contains("secret") ?: false)
    }

    @Test
    fun testSafeLogSanitization() {
        val inputWithKey = "Connecting to Supabase key: sb_publishable_XtQN9HZAmLO8LDllURZOww_5LPbAnjP"
        val sanitizedKey = SafeLog.sanitize(inputWithKey)
        assertFalse(sanitizedKey.contains("sb_publishable_XtQN9HZAmLO8LDllURZOww_5LPbAnjP"))
        assertTrue(sanitizedKey.contains("[REDACTED_PUBLISHABLE_KEY]"))

        val inputWithPassword = "Login attempt with password=\"SecretP@ssword!123\""
        val sanitizedPassword = SafeLog.sanitize(inputWithPassword)
        assertFalse(sanitizedPassword.contains("SecretP@ssword!123"))
        assertTrue(sanitizedPassword.contains("password=[REDACTED]"))

        val inputWithBearer = "Request headers: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.doNotLeak"
        val sanitizedBearer = SafeLog.sanitize(inputWithBearer)
        assertFalse(sanitizedBearer.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"))
        assertTrue(sanitizedBearer.contains("Bearer [REDACTED]") || sanitizedBearer.contains("[JWT_REDACTED]"))
    }
}
