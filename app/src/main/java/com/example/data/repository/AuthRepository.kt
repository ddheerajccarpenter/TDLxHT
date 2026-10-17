package com.example.data.repository

import com.example.data.service.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    private val auth get() = SupabaseClient.client.auth

    /** Returns the currently signed-in user, or null if not signed in. */
    val currentUser: Flow<UserInfo?> = auth.sessionStatus.map { status ->
        (status as? SessionStatus.Authenticated)?.session?.user
    }

    /** Sign-up with email + password + optional display name. */
    suspend fun signUp(email: String, password: String, displayName: String): Result<Unit> {
        return runCatching {
            auth.signUpWith(
                provider = Email,
                redirectUrl = "${com.example.BuildConfig.APP_WEB_URL.trimEnd('/')}/auth/callback"
            ) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    if (displayName.isNotBlank()) put("display_name", displayName)
                }
            }
        }
    }

    /** Sign-in with email + password. */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return runCatching {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    /** Sign out and clear local session. */
    suspend fun signOut(): Result<Unit> {
        return runCatching { auth.signOut() }
    }

    /** Send password reset email. */
    suspend fun resetPassword(email: String): Result<Unit> {
        return runCatching {
            auth.resetPasswordForEmail(
                email = email,
                redirectUrl = "${com.example.BuildConfig.APP_WEB_URL.trimEnd('/')}/auth/callback"
            )
        }
    }

    /** Resend email confirmation for unconfirmed accounts. */
    suspend fun resendConfirmationEmail(email: String): Result<Unit> {
        return runCatching {
            auth.resendEmail(
                type = io.github.jan.supabase.auth.OtpType.Email.SIGNUP,
                email = email
            )
        }
    }

    /** True if a session is currently active. */
    fun isSignedIn(): Boolean = auth.currentSessionOrNull() != null

    /** Retrieve the current user's ID. */
    fun currentUserId(): String? = auth.currentSessionOrNull()?.user?.id?.toString()

    /** Deletes the authenticated user account and all cascaded cloud data. */
    suspend fun deleteAccount(): Result<Unit> {
        return runCatching {
            try {
                SupabaseClient.client.postgrest.rpc("delete_current_user_data")
            } catch (_: Exception) {}
            auth.signOut()
        }
    }
}
